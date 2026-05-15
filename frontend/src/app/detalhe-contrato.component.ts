import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiFeedbackService } from './api-feedback.service';
import { totalSetoresRelacionados, ultimaVersaoRelacionada } from './detalhe-operacional.utils';
import { ContratoArquivoResponse, PainelOperacionalApi, ContratoDetalhe } from './painel-operacional-api';
import { AuthSessionService } from './auth-session.service';

@Component({
  selector: 'app-detalhe-contrato',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './detalhe-contrato.component.html',
  styleUrl: './detalhe-contrato.component.scss'
})
export class DetalheContratoComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(PainelOperacionalApi);
  private readonly apiFeedback = inject(ApiFeedbackService);
  private readonly authSession = inject(AuthSessionService);

  protected readonly detalhe = signal<ContratoDetalhe | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal('');
  protected readonly salvando = signal(false);
  protected readonly enviandoArquivo = signal(false);
  protected readonly mensagemSucesso = signal('');
  protected readonly erroArquivo = signal('');
  protected readonly particularidades = signal('');
  protected readonly documentacao = signal('');
  protected readonly ehAdmin = signal(this.authSession.session()?.roles.includes('ROLE_ADMIN') ?? false);
  protected readonly podeSalvarDocumentacao = computed(() => {
    const dados = this.detalhe();
    if (!dados || !this.ehAdmin()) {
      return false;
    }

    return this.normalizarTexto(this.particularidades()) !== this.normalizarTexto(dados.contrato.particularidades ?? '')
      || this.normalizarTexto(this.documentacao()) !== this.normalizarTexto(dados.contrato.documentacao ?? '');
  });

  constructor() {
    const contratoId = Number(this.route.snapshot.paramMap.get('id'));

    if (!contratoId) {
      this.carregando.set(false);
      this.erro.set('Contrato invalido.');
      return;
    }

    this.api.carregarContratoDetalhe(contratoId)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (response) => {
          if (!response.sucesso || !response.dados?.contrato) {
            this.detalhe.set(null);
            this.erro.set(response.mensagem || 'Nao foi possivel carregar o detalhe do contrato.');
            return;
          }

          const detalhe = {
            ...response.dados,
            servicos: response.dados.servicos ?? [],
            arquivos: response.dados.arquivos ?? []
          };

          this.detalhe.set(detalhe);
          this.particularidades.set(detalhe.contrato.particularidades ?? '');
          this.documentacao.set(detalhe.contrato.documentacao ?? '');
        },
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, 'Nao foi possivel carregar o detalhe do contrato.'));
        }
      });
  }

  protected salvarDocumentacao(): void {
    const dados = this.detalhe();

    if (!dados || !this.ehAdmin()) {
      return;
    }

    this.salvando.set(true);
    this.mensagemSucesso.set('');
    this.erro.set('');

    this.api.atualizarContrato(dados.contrato.id, {
      nome: dados.contrato.nome,
      grupo: dados.contrato.grupo,
      centralUrl: dados.contrato.centralUrl,
      particularidades: this.normalizarTexto(this.particularidades()),
      documentacao: this.normalizarTexto(this.documentacao()),
      ativo: dados.contrato.ativo
    })
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: (response) => {
          this.detalhe.set({
            ...dados,
            contrato: response.dados
          });
          this.mensagemSucesso.set('Documentacao atualizada com sucesso.');
        },
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, 'Nao foi possivel salvar a documentacao do contrato.'));
        }
      });
  }

  protected adicionarArquivo(event: Event): void {
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.[0];
    input.value = '';

    const dados = this.detalhe();
    if (!arquivo || !dados || !this.ehAdmin()) {
      return;
    }

    this.enviandoArquivo.set(true);
    this.erroArquivo.set('');

    this.api.uploadContratoArquivo(dados.contrato.id, arquivo)
      .pipe(finalize(() => this.enviandoArquivo.set(false)))
      .subscribe({
        next: (response) => {
          if (!response.sucesso) {
            this.erroArquivo.set(response.mensagem || 'Nao foi possivel adicionar o arquivo.');
            return;
          }

          this.detalhe.set({
            ...dados,
            arquivos: [response.dados, ...(dados.arquivos ?? [])]
          });
        },
        error: (error) => {
          this.erroArquivo.set(this.apiFeedback.mensagem(error, 'Nao foi possivel adicionar o arquivo.'));
        }
      });
  }

  protected abrirArquivo(arquivo: ContratoArquivoResponse): void {
    const contratoId = this.detalhe()?.contrato.id;
    if (!contratoId) {
      return;
    }

    this.api.abrirContratoArquivo(contratoId, arquivo.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank', 'noopener');
        setTimeout(() => URL.revokeObjectURL(url), 30000);
      },
      error: (error) => {
        this.erroArquivo.set(this.apiFeedback.mensagem(error, 'Nao foi possivel abrir o arquivo.'));
      }
    });
  }

  protected baixarArquivo(arquivo: ContratoArquivoResponse): void {
    const contratoId = this.detalhe()?.contrato.id;
    if (!contratoId) {
      return;
    }

    this.api.baixarContratoArquivo(contratoId, arquivo.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = arquivo.nome;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: (error) => {
        this.erroArquivo.set(this.apiFeedback.mensagem(error, 'Nao foi possivel baixar o arquivo.'));
      }
    });
  }

  protected tamanhoArquivo(tamanho: number): string {
    if (tamanho < 1024) {
      return `${tamanho} B`;
    }

    if (tamanho < 1024 * 1024) {
      return `${(tamanho / 1024).toFixed(1)} KB`;
    }

    return `${(tamanho / 1024 / 1024).toFixed(1)} MB`;
  }

  protected dicaEdicao(): string {
    if (!this.ehAdmin()) {
      return '';
    }

    if (this.salvando()) {
      return 'Salvando alteracoes na documentacao do contrato.';
    }

    return this.podeSalvarDocumentacao()
      ? 'Existem alteracoes pendentes para salvar.'
      : 'Edite particularidades ou documentacao para liberar o salvamento.';
  }

  private normalizarTexto(valor: string): string {
    return valor.trim();
  }

  protected totalSetores(dados: ContratoDetalhe): number {
    return totalSetoresRelacionados(dados.servicos);
  }

  protected ultimaVersao(dados: ContratoDetalhe): string {
    return ultimaVersaoRelacionada(dados.servicos);
  }

}
