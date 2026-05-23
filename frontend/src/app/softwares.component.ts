import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { finalize } from 'rxjs';
import { ApiFeedbackService } from './api-feedback.service';
import { AuthSessionService } from './auth-session.service';
import { ActionIconComponent } from './components/action-icon.component';
import { PainelOperacionalApi } from './painel-operacional-api';
import { SoftwareViewModel, mapearSoftwareViewModel } from './softwares.models';

interface SoftwareFormModel {
  id: number | null;
  nome: string;
  descricaoSoftware: string;
  versaoReferencia: string;
  linkSoftware: string;
  ativo: boolean;
}

const SOFTWARE_FORM_VAZIO: SoftwareFormModel = {
  id: null,
  nome: '',
  descricaoSoftware: '',
  versaoReferencia: '',
  linkSoftware: '',
  ativo: true
};

@Component({
  selector: 'app-softwares',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatSelectModule, ActionIconComponent],
  templateUrl: './softwares.component.html',
  styleUrl: './softwares.component.scss'
})
export class SoftwaresComponent {
  private readonly api = inject(PainelOperacionalApi);
  private readonly apiFeedback = inject(ApiFeedbackService);
  private readonly authSession = inject(AuthSessionService);

  protected readonly itens = signal<SoftwareViewModel[]>([]);
  protected readonly carregando = signal(true);
  protected readonly salvando = signal(false);
  protected readonly erro = signal('');
  protected readonly mensagem = signal('');
  protected readonly softwareSelecionadoId = signal(0);
  protected readonly formularioAberto = signal(false);
  protected readonly softwareForm = signal<SoftwareFormModel>({ ...SOFTWARE_FORM_VAZIO });
  protected readonly ehAdmin = computed(() => this.authSession.session()?.roles.includes('ROLE_ADMIN') ?? false);
  protected readonly softwareSelecionado = computed(() =>
    this.itensVisuais().find((item) => item.id === this.softwareSelecionadoId()) ?? null
  );
  protected readonly itensVisuais = computed(() => this.itens());
  protected readonly sistemasMapeados = computed(() => this.itens().length);
  protected readonly atualizadosCount = computed(() => this.itens().filter((item) => item.status === 'Disponível').length);
  protected readonly pendentesCount = computed(() => this.itens().filter((item) => item.status !== 'Disponível').length);
  protected readonly ultimaSincronizacao = computed(() => this.itens()[0]?.ultimaAtualizacao ?? '-');

  constructor() {
    this.carregarSoftwares();
  }

  protected selecionarSoftware(softwareId: number): void {
    this.softwareSelecionadoId.set(softwareId);
  }

  protected abrirNovoSoftware(): void {
    this.softwareForm.set({ ...SOFTWARE_FORM_VAZIO });
    this.formularioAberto.set(true);
    this.limparFeedback();
  }

  protected editarSoftware(software: SoftwareViewModel): void {
    this.softwareForm.set({
      id: software.id,
      nome: software.nome,
      descricaoSoftware: software.descricaoSoftware ?? '',
      versaoReferencia: software.versaoReferencia ?? '',
      linkSoftware: software.linkSoftware ?? '',
      ativo: software.ativo
    });
    this.formularioAberto.set(true);
    this.limparFeedback();
  }

  protected fecharFormulario(): void {
    this.formularioAberto.set(false);
  }

  protected atualizarCampo<K extends keyof SoftwareFormModel>(campo: K, valor: SoftwareFormModel[K]): void {
    this.softwareForm.update((form) => ({ ...form, [campo]: valor }));
  }

  protected formularioValido(): boolean {
    return Boolean(this.softwareForm().nome.trim());
  }

  protected salvarSoftware(): void {
    if (!this.formularioValido()) {
      this.erro.set('Informe o nome do software.');
      return;
    }

    const form = this.softwareForm();
    const payload = {
      nome: form.nome.trim(),
      descricaoSoftware: form.descricaoSoftware.trim(),
      versaoReferencia: form.versaoReferencia.trim(),
      linkSoftware: form.linkSoftware.trim(),
      ativo: form.ativo
    };
    const request = form.id == null
      ? this.api.criarSoftware(payload)
      : this.api.atualizarSoftware(form.id, payload);

    this.salvando.set(true);
    this.limparFeedback();
    request
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: () => {
          this.mensagem.set(form.id == null ? 'Software criado com sucesso.' : 'Software atualizado com sucesso.');
          this.formularioAberto.set(false);
          this.carregarSoftwares(false);
        },
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, 'Não foi possível salvar o software.'));
        }
      });
  }

  protected alternarSoftware(software: SoftwareViewModel): void {
    const request = software.ativo
      ? this.api.inativarSoftware(software.id)
      : this.api.ativarSoftware(software.id);

    this.salvando.set(true);
    this.limparFeedback();
    request
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: () => {
          this.mensagem.set(software.ativo ? 'Software inativado com sucesso.' : 'Software ativado com sucesso.');
          this.carregarSoftwares(false);
        },
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, 'Não foi possível atualizar o status do software.'));
        }
      });
  }

  protected baixarSoftware(software: SoftwareViewModel): void {
    if (!software.downloadDisponivel) {
      return;
    }

    this.api.baixarSoftware(software.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = software.origemArquivo || `${software.nome}.bin`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.mensagem.set('Download iniciado com sucesso.');
      },
      error: (error) => {
        this.erro.set(this.apiFeedback.mensagem(error, 'Não foi possível baixar o artefato.'));
      }
    });
  }

  private carregarSoftwares(exibirCarregando = true): void {
    if (exibirCarregando) {
      this.carregando.set(true);
    }

    this.api.carregarSoftwares()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (response) => {
          const ultimaAtualizacao = this.formatarUltimaAtualizacao();
          this.itens.set(response.dados.map((item) => mapearSoftwareViewModel(item, ultimaAtualizacao)));
        },
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, 'Não foi possível carregar os sistemas.'));
        }
      });
  }

  private formatarUltimaAtualizacao(): string {
    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(new Date());
  }

  private limparFeedback(): void {
    this.erro.set('');
    this.mensagem.set('');
  }
}
