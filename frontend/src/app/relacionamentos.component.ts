import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiFeedbackService } from './api-feedback.service';
import { AuthSessionService } from './auth-session.service';
import { FiltrosOperacionaisComponent } from './components/filtros-operacionais.component';
import { ResumoCardsComponent } from './components/resumo-cards.component';
import { TabelaOperacionalComponent } from './components/tabela-operacional.component';
import {
  PainelOperacional,
  PainelOperacionalApi,
  PainelRequest
} from './painel-operacional-api';

const MENSAGEM_CARGA = 'Não foi possível carregar os dados.';
const MENSAGEM_AUTENTICACAO = 'Entre com suas credenciais para consultar os relacionamentos operacionais.';

@Component({
  selector: 'app-relacionamentos',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FiltrosOperacionaisComponent,
    ResumoCardsComponent,
    TabelaOperacionalComponent
  ],
  templateUrl: './relacionamentos.component.html',
  styleUrl: './relacionamentos.component.scss'
})
export class RelacionamentosComponent {
  private readonly api = inject(PainelOperacionalApi);
  private readonly apiFeedback = inject(ApiFeedbackService);
  private readonly authSession = inject(AuthSessionService);

  protected readonly grupo = signal('');
  protected readonly setor = signal('');
  protected readonly contratoIds = signal<number[]>([]);
  protected readonly servicoIds = signal<number[]>([]);

  protected readonly painel = signal<PainelOperacional | null>(null);
  protected readonly ultimaAtualizacao = signal<Date | null>(null);
  protected readonly carregando = signal(false);
  protected readonly erro = signal('');

  protected readonly autenticado = computed(() => this.authSession.isAuthenticated());
  protected readonly ehAdmin = computed(() => this.authSession.session()?.roles.includes('ROLE_ADMIN') ?? false);
  protected readonly painelVisual = computed(() => this.painel());
  protected readonly saudacaoPainel = computed(() => {
    const painel = this.painel();
    if (!painel) {
      return 'Consulte contratos, serviços, versões e setores em uma leitura cruzada única.';
    }

    if (!painel.linhas.length) {
      return 'Nenhum relacionamento encontrado.';
    }

    return `${painel.resumo.totalRelacionamentos} registros ativos cruzados com ${painel.resumo.totalSetores} setores.`;
  });
  protected readonly ultimaAtualizacaoFormatada = computed(() => this.formatarUltimaAtualizacao(this.ultimaAtualizacao()));

  constructor() {
    if (this.autenticado()) {
      this.carregarPainel();
      return;
    }

    this.erro.set(MENSAGEM_AUTENTICACAO);
  }

  protected carregarPainel(): void {
    if (!this.authSession.isAuthenticated()) {
      this.erro.set(MENSAGEM_AUTENTICACAO);
      return;
    }

    this.carregando.set(true);
    this.erro.set('');

    this.api
      .carregarPainel(this.criarFiltrosPainel())
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (response) => {
          this.painel.set(response.dados);
          this.ultimaAtualizacao.set(new Date());
        },
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, MENSAGEM_CARGA));
        }
      });
  }

  protected atualizarContratosSelecionados(selecionados: number[]): void {
    this.contratoIds.set(selecionados);
  }

  protected atualizarServicosSelecionados(selecionados: number[]): void {
    this.servicoIds.set(selecionados);
  }

  protected limparFiltros(): void {
    this.grupo.set('');
    this.setor.set('');
    this.contratoIds.set([]);
    this.servicoIds.set([]);
    this.carregarPainel();
  }

  private criarFiltrosPainel(): PainelRequest {
    return {
      contratoIds: this.contratoIds(),
      servicoIds: this.servicoIds(),
      grupo: this.grupo(),
      setor: this.setor(),
      contratoNome: '',
      servicoNome: ''
    };
  }

  private formatarUltimaAtualizacao(data: Date | null): string {
    if (!data) {
      return 'Atualização pendente';
    }

    return `Dados carregados em ${data.toLocaleDateString('pt-BR')} ${data.toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit'
    })}`;
  }

}
