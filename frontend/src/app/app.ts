import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, finalize } from 'rxjs';
import { ApiFeedbackService } from './api-feedback.service';
import { AuthSessionService, DEFAULT_API_URL } from './auth-session.service';
import { LoginPanelComponent } from './components/login-panel.component';
import { PainelOperacional, PainelOperacionalApi, PainelRequest } from './painel-operacional-api';

const MENSAGEM_LOGIN_FALHOU = 'Nao foi possivel iniciar a sessao.';
const MENSAGEM_PAINEL_FALHOU = 'Nao foi possivel carregar o painel. Verifique a API e as credenciais.';
const MENSAGEM_RESTAURACAO_FALHOU = 'Nao foi possivel restaurar a sessao.';
const MENSAGEM_AUTENTICACAO_NECESSARIA = 'Entre com suas credenciais para consultar a API.';
const LEGACY_API_URLS = new Set([
  'http://localhost:8080',
  'http://127.0.0.1:8080'
]);

@Component({
  selector: 'app-root',
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    LoginPanelComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private readonly api = inject(PainelOperacionalApi);
  private readonly apiFeedback = inject(ApiFeedbackService);
  private readonly authSession = inject(AuthSessionService);
  private readonly router = inject(Router);

  protected readonly baseUrl = signal(this.authSession.preferences().baseUrl ?? DEFAULT_API_URL);
  protected readonly username = signal(this.authSession.preferences().username ?? '');
  protected readonly password = signal('');

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
  protected readonly sessaoExpiraEm = computed(() => this.authSession.session()?.expiraEm ?? null);
  protected readonly sessaoRestante = computed(() => this.formatarTempoRestante(this.sessaoExpiraEm()));
  protected readonly rotaAtual = signal(this.obterRotaAtualInicial());
  protected readonly exibeHome = computed(() => this.rotaSemParametros() === '/' || this.rotaSemParametros() === '');
  protected readonly exibeLogin = computed(() => this.rotaSemParametros() === '/login');
  protected readonly iniciaisUsuario = computed(() => this.obterIniciais(this.username()));
  protected readonly perfilUsuario = computed(() => (this.ehAdmin() ? 'Administrador' : 'Operacional'));
  protected readonly ultimaAtualizacaoFormatada = computed(() => this.formatarUltimaAtualizacao(this.ultimaAtualizacao()));
  protected readonly saudacaoPainel = computed(() => {
    const painel = this.painel();
    if (!painel) {
      return 'Base aguardando autenticacao para consolidar contratos, servicos e relacionamentos.';
    }

    if (!painel.linhas.length) {
      return 'Conexao valida. Ajuste os filtros para ampliar a leitura operacional.';
    }

    return `${painel.resumo.totalRelacionamentos} registros ativos cruzados com ${painel.resumo.totalSetores} setores.`;
  });

  constructor() {
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => this.rotaAtual.set(event.urlAfterRedirects));

    queueMicrotask(() => this.rotaAtual.set(this.obterRotaAtualInicial()));

    if (this.authSession.isAuthenticated()) {
      this.carregarSessaoEConteudo();
    }
  }

  protected entrar(): void {
    this.baseUrl.set(this.normalizeBaseUrl(this.baseUrl()));
    this.authSession.rememberConnection(this.baseUrl(), this.username());
    this.prepararCarregamento();

    this.api.login({
      username: this.username().trim(),
      password: this.password()
    }).subscribe({
      next: (response) => {
        this.authSession.save({
          baseUrl: this.baseUrl(),
          username: response.dados.username,
          token: response.dados.token,
          roles: response.dados.roles,
          expiraEm: response.dados.expiraEm
        });
        this.password.set('');
        this.router.navigateByUrl('/relacionamentos');
        this.carregarPainel();
      },
      error: (error) => {
        this.carregando.set(false);
        this.erro.set(this.extrairMensagemDeErro(error, MENSAGEM_LOGIN_FALHOU));
      }
    });
  }

  protected sair(): void {
    if (this.authSession.isAuthenticated()) {
      this.api.logout().subscribe({
        error: () => {
          // Limpa a sessao local mesmo quando a API nao responde no logout.
        }
      });
    }

    this.limparSessaoLocal();
    this.painel.set(null);
    this.ultimaAtualizacao.set(null);
    this.erro.set('');
    this.router.navigateByUrl('/login');
  }

  protected usarApiPadrao(): void {
    this.baseUrl.set(DEFAULT_API_URL);
    this.authSession.rememberConnection(DEFAULT_API_URL, this.username());
    this.erro.set('');
  }

  protected carregarPainel(): void {
    if (!this.authSession.isAuthenticated()) {
      this.erro.set(MENSAGEM_AUTENTICACAO_NECESSARIA);
      return;
    }

    this.prepararCarregamento();

    this.api
      .carregarPainel(this.criarFiltrosPainel())
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (response) => {
          this.painel.set(response.dados);
          this.ultimaAtualizacao.set(new Date());
        },
        error: (error) => {
          const mensagem = this.extrairMensagemDeErro(error, MENSAGEM_PAINEL_FALHOU);
          this.erro.set(mensagem);
          if (error?.status === 401) {
            this.limparSessaoLocal();
          }
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

  protected async abrirSecaoHome(secaoId: string): Promise<void> {
    if (!this.exibeHome()) {
      await this.router.navigateByUrl('/');
    }

    setTimeout(() => {
      document.getElementById(secaoId)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 0);
  }

  private carregarSessaoEConteudo(): void {
    this.prepararCarregamento();

    this.api.carregarSessao().subscribe({
      next: (response) => {
        this.atualizarSessaoAtiva(response.dados.username, response.dados.roles, response.dados.expiraEm);
        this.carregarPainel();
      },
      error: (error) => {
        this.carregando.set(false);
        this.erro.set(this.extrairMensagemDeErro(error, MENSAGEM_RESTAURACAO_FALHOU));
        this.limparSessaoLocal();
      }
    });
  }

  private prepararCarregamento(): void {
    this.carregando.set(true);
    this.erro.set('');
  }

  private limparSessaoLocal(): void {
    this.authSession.clear();
    this.password.set('');
  }

  private atualizarSessaoAtiva(username: string, roles: string[], expiraEm: string | null): void {
    const sessao = this.authSession.session();
    if (!sessao) {
      return;
    }

    this.authSession.save({
      ...sessao,
      username,
      roles,
      expiraEm
    });
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

  private extrairMensagemDeErro(error: { status?: number; error?: { mensagem?: string } } | null | undefined, fallback: string): string {
    if (error?.status === 0) {
      return `Nao foi possivel conectar na API em ${this.normalizeBaseUrl(this.baseUrl())}. Confirme se o backend demo esta ligado na porta 8091.`;
    }

    return this.apiFeedback.mensagem(error, fallback);
  }

  private normalizeBaseUrl(baseUrl: string): string {
    const normalizedValue = baseUrl.trim().replace(/\/+$/, '');
    if (!normalizedValue || LEGACY_API_URLS.has(normalizedValue)) {
      return DEFAULT_API_URL;
    }

    return normalizedValue;
  }

  private obterRotaAtualInicial(): string {
    return globalThis.location?.pathname || this.router.url;
  }

  private rotaSemParametros(): string {
    return this.rotaAtual().split('?')[0].split('#')[0];
  }

  private formatarTempoRestante(expiraEm: string | null): string {
    if (!expiraEm) {
      return 'Sem informacao de expiracao';
    }

    const expiracao = new Date(expiraEm).getTime();
    const restanteMs = expiracao - Date.now();
    if (Number.isNaN(expiracao) || restanteMs <= 0) {
      return 'Expirando agora';
    }

    const totalMinutos = Math.ceil(restanteMs / 60000);
    if (totalMinutos < 60) {
      return `${totalMinutos} min restantes`;
    }

    const horas = Math.floor(totalMinutos / 60);
    const minutos = totalMinutos % 60;
    if (!minutos) {
      return `${horas} h restantes`;
    }

    return `${horas} h ${minutos} min restantes`;
  }

  private obterIniciais(username: string): string {
    const valor = username.trim();
    if (!valor) {
      return 'AT';
    }

    const partes = valor.split(/[.\s_-]+/).filter(Boolean);
    if (partes.length === 1) {
      return partes[0].slice(0, 2).toUpperCase();
    }

    return `${partes[0][0] ?? ''}${partes[1][0] ?? ''}`.toUpperCase();
  }

  private formatarUltimaAtualizacao(data: Date | null): string {
    if (!data) {
      return 'Aguardando primeira leitura';
    }

    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short'
    }).format(data);
  }
}
