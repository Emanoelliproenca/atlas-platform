import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiFeedbackService } from './api-feedback.service';
import { AuthSessionService } from './auth-session.service';
import { PainelOperacionalApi } from './painel-operacional-api';
import { SoftwareViewModel, mapearSoftwareViewModel } from './softwares.models';

@Component({
  selector: 'app-softwares',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './softwares.component.html',
  styleUrl: './softwares.component.scss'
})
export class SoftwaresComponent {
  private readonly api = inject(PainelOperacionalApi);
  private readonly apiFeedback = inject(ApiFeedbackService);
  private readonly authSession = inject(AuthSessionService);

  protected readonly itens = signal<SoftwareViewModel[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal('');
  protected readonly mensagem = signal('');
  protected readonly softwareSelecionadoId = signal(0);
  protected readonly ehAdmin = computed(() => this.authSession.session()?.roles.includes('ROLE_ADMIN') ?? false);
  protected readonly softwareSelecionado = computed(() =>
    this.itens().find((item) => item.id === this.softwareSelecionadoId()) ?? null
  );
  protected readonly atualizadosCount = computed(() =>
    this.itens().filter((item) => item.status === 'Disponivel').length
  );
  protected readonly pendentesCount = computed(() =>
    this.itens().filter((item) => item.status !== 'Disponivel').length
  );
  protected readonly ultimaSincronizacao = computed(() => this.itens()[0]?.ultimaAtualizacao ?? '-');

  constructor() {
    this.api.carregarSoftwares()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (response) => {
          const ultimaAtualizacao = this.formatarUltimaAtualizacao();
          this.itens.set(response.dados.map((item) => mapearSoftwareViewModel(item, ultimaAtualizacao)));
        },
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, 'Nao foi possivel carregar os softwares.'));
        }
      });
  }

  protected selecionarSoftware(softwareId: number): void {
    this.softwareSelecionadoId.set(softwareId);
  }

  protected baixarSoftware(software: SoftwareViewModel): void {
    if (!software.downloadDisponivel) {
      return;
    }

    this.mensagem.set(
      `Download preparado para ${software.nome}. Integracao com a central externa sera conectada ao endpoint /softwares/${software.id}/download.`
    );
    this.erro.set('');
  }

  protected sincronizarVersoes(): void {
    this.mensagem.set('Sincronizacao futura preparada. Esta acao sera ligada a uma central externa de versoes.');
    this.erro.set('');
  }

  private formatarUltimaAtualizacao(): string {
    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(new Date());
  }
}
