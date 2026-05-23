import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import { Observable, finalize, forkJoin } from 'rxjs';
import { ApiFeedbackService } from './api-feedback.service';
import { AuthSessionService } from './auth-session.service';
import {
  ABAS_CADASTRO,
  DRAWER_ENTITY_LABELS,
  DRAWER_SECTION_LABELS,
  AbaCadastro,
  ContratoFormModel,
  DrawerEntidade,
  DrawerModo,
  FiltroStatus,
  RelacionamentoFormModel,
  ServicoFormModel,
  criarContratoVazio,
  criarRelacionamentoVazio,
  criarServicoVazio,
  mapearContratoParaForm,
  mapearRelacionamentoParaForm,
  mapearServicoParaForm,
  resumirObservacao
} from './cadastros.models';
import { ActionIconComponent } from './components/action-icon.component';
import {
  contarContratosAtivosPorServico,
  contarInativos,
  criarPayloadContrato,
  criarPayloadRelacionamento,
  criarPayloadServico,
  filtrarContratos,
  filtrarRelacionamentos,
  filtrarServicos,
  formatarUltimaAtualizacao,
  gruposOrdenados,
  relacionamentoDuplicado,
  setoresOrdenados
} from './cadastros.utils';
import {
  ContratoServicoResponse,
  ContratoResponse,
  PainelOperacionalApi,
  ServicoResponse
} from './painel-operacional-api';

const MENSAGEM_REGISTRO_SALVO = 'Registro salvo com sucesso.';
const MENSAGEM_ALTERACOES_SALVAS = 'Alterações salvas com sucesso.';
const MENSAGEM_REGISTRO_ATIVADO = 'Registro ativado com sucesso.';
const MENSAGEM_REGISTRO_INATIVADO = 'Registro inativado com sucesso.';
const MENSAGEM_OPERACAO_INVALIDA = 'Não foi possível concluir a operação. Verifique os dados e tente novamente.';
const MENSAGEM_RELACIONAMENTO_DUPLICADO =
  'Não foi possível concluir a operação. Este serviço já está vinculado a este contrato.';
const MENSAGEM_CARGA_ADMINISTRATIVA = 'Não foi possível carregar a base administrativa.';

@Component({
  selector: 'app-cadastros',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatSelectModule, ActionIconComponent],
  templateUrl: './cadastros.component.html',
  styleUrl: './cadastros.component.scss'
})
export class CadastrosComponent {
  private readonly api = inject(PainelOperacionalApi);
  private readonly apiFeedback = inject(ApiFeedbackService);
  private readonly authSession = inject(AuthSessionService);
  private readonly router = inject(Router);

  protected readonly abas = ABAS_CADASTRO;

  protected readonly abaAtiva = signal<AbaCadastro>('contratos');
  protected readonly contratos = signal<ContratoResponse[]>([]);
  protected readonly servicos = signal<ServicoResponse[]>([]);
  protected readonly relacionamentos = signal<ContratoServicoResponse[]>([]);
  protected readonly carregando = signal(false);
  protected readonly salvando = signal(false);
  protected readonly mensagem = signal('');
  protected readonly erro = signal('');
  protected readonly ultimaAtualizacao = signal(new Date());

  protected readonly drawerAberto = signal(false);
  protected readonly drawerEntidade = signal<DrawerEntidade>('contrato');
  protected readonly drawerModo = signal<DrawerModo>('novo');

  protected readonly contratoFiltroNome = signal('');
  protected readonly contratoFiltroGrupo = signal('');
  protected readonly contratoFiltroStatus = signal<FiltroStatus>('todos');
  protected readonly servicoFiltroNome = signal('');
  protected readonly servicoFiltroStatus = signal<FiltroStatus>('todos');
  protected readonly relacionamentoFiltroContratoId = signal(0);
  protected readonly relacionamentoFiltroServicoId = signal(0);
  protected readonly relacionamentoFiltroSetor = signal('');
  protected readonly relacionamentoFiltroStatus = signal<FiltroStatus>('todos');

  protected contratoForm: ContratoFormModel = criarContratoVazio();
  protected servicoForm: ServicoFormModel = criarServicoVazio();
  protected relacionamentoForm: RelacionamentoFormModel = criarRelacionamentoVazio();

  protected readonly ehAdmin = computed(() => this.authSession.session()?.roles.includes('ROLE_ADMIN') ?? false);
  protected readonly contratosAtivos = computed(() => this.contratos().filter((item) => item.ativo));
  protected readonly servicosAtivos = computed(() => this.servicos().filter((item) => item.ativo));
  protected readonly contratosAtivosCount = computed(() => this.contratos().filter((item) => item.ativo).length);
  protected readonly servicosAtivosCount = computed(() => this.servicos().filter((item) => item.ativo).length);
  protected readonly relacionamentosAtivosCount = computed(() => this.relacionamentos().filter((item) => item.ativo).length);
  protected readonly inativosCount = computed(() => contarInativos(this.contratos(), this.servicos(), this.relacionamentos()));
  protected readonly gruposDisponiveis = computed(() => gruposOrdenados(this.contratos()));
  protected readonly setoresDisponiveis = computed(() => setoresOrdenados(this.relacionamentos()));
  protected readonly contratosFiltrados = computed(() =>
    filtrarContratos(
      this.contratos(),
      this.contratoFiltroNome(),
      this.contratoFiltroGrupo(),
      this.contratoFiltroStatus()
    )
  );
  protected readonly servicosFiltrados = computed(() =>
    filtrarServicos(
      this.servicos(),
      this.servicoFiltroNome(),
      this.servicoFiltroStatus()
    )
  );
  protected readonly relacionamentosFiltrados = computed(() =>
    filtrarRelacionamentos(
      this.relacionamentos(),
      this.relacionamentoFiltroContratoId(),
      this.relacionamentoFiltroServicoId(),
      this.relacionamentoFiltroSetor(),
      this.relacionamentoFiltroStatus()
    )
  );
  protected readonly drawerSomenteLeitura = computed(() => this.drawerModo() === 'visualizar');
  protected readonly drawerEntidadeTitulo = computed(() => DRAWER_SECTION_LABELS[this.drawerEntidade()]);

  constructor() {
    if (this.ehAdmin()) {
      this.carregarBaseAdministrativa();
    }
  }

  protected drawerTitulo(): string {
    const entidade = this.drawerEntidade();
    const modo = this.drawerModo();

    if (modo === 'novo') {
      return `Novo ${DRAWER_ENTITY_LABELS[entidade]}`;
    }

    if (modo === 'visualizar') {
      return `Visualizar ${DRAWER_ENTITY_LABELS[entidade]}`;
    }

    return `Editar ${DRAWER_ENTITY_LABELS[entidade]}`;
  }

  protected drawerBotaoSalvar(): string {
    return this.drawerModo() === 'editar'
      ? 'Salvar alterações'
      : `Salvar ${this.drawerEntidade()}`;
  }

  protected drawerFormValido(): boolean {
    if (this.drawerEntidade() === 'contrato') {
      return Boolean(this.contratoForm.nome.trim() && this.contratoForm.grupo.trim());
    }

    if (this.drawerEntidade() === 'servico') {
      return Boolean(this.servicoForm.nome.trim());
    }

    return this.relacionamentoForm.contratoId > 0
      && this.relacionamentoForm.servicoId > 0
      && !this.relacionamentoDuplicado();
  }

  protected abrirDrawerContrato(modo: DrawerModo, item?: ContratoResponse): void {
    this.drawerEntidade.set('contrato');
    this.drawerModo.set(modo);
    this.contratoForm = mapearContratoParaForm(item);
    this.abrirDrawer();
  }

  protected abrirDrawerServico(modo: DrawerModo, item?: ServicoResponse): void {
    this.drawerEntidade.set('servico');
    this.drawerModo.set(modo);
    this.servicoForm = mapearServicoParaForm(item);
    this.abrirDrawer();
  }

  protected abrirDrawerRelacionamento(modo: DrawerModo, item?: ContratoServicoResponse): void {
    this.drawerEntidade.set('relacionamento');
    this.drawerModo.set(modo);
    this.relacionamentoForm = mapearRelacionamentoParaForm(item);
    this.abrirDrawer();
  }

  protected fecharDrawer(): void {
    this.drawerAberto.set(false);
  }

  protected salvarDrawer(): void {
    if (this.drawerSomenteLeitura()) {
      return;
    }

    switch (this.drawerEntidade()) {
      case 'contrato':
        this.salvarContrato();
        return;
      case 'servico':
        this.salvarServico();
        return;
      default:
        this.salvarRelacionamento();
    }
  }

  protected alternarContrato(item: ContratoResponse): void {
    this.executarAlternancia(item.ativo, this.api.inativarContrato(item.id), this.api.ativarContrato(item.id));
  }

  protected alternarServico(item: ServicoResponse): void {
    this.executarAlternancia(item.ativo, this.api.inativarServico(item.id), this.api.ativarServico(item.id));
  }

  protected alternarRelacionamento(item: ContratoServicoResponse): void {
    this.executarAlternancia(
      item.ativo,
      this.api.inativarRelacionamento(item.id),
      this.api.ativarRelacionamento(item.id)
    );
  }

  protected limparFiltrosContrato(): void {
    this.contratoFiltroNome.set('');
    this.contratoFiltroGrupo.set('');
    this.contratoFiltroStatus.set('todos');
  }

  protected limparFiltrosServico(): void {
    this.servicoFiltroNome.set('');
    this.servicoFiltroStatus.set('todos');
  }

  protected limparFiltrosRelacionamento(): void {
    this.relacionamentoFiltroContratoId.set(0);
    this.relacionamentoFiltroServicoId.set(0);
    this.relacionamentoFiltroSetor.set('');
    this.relacionamentoFiltroStatus.set('todos');
  }

  protected resumirObservacao(valor: string): string {
    return resumirObservacao(valor);
  }

  protected totalContratosPorServico(servicoId: number): number {
    return contarContratosAtivosPorServico(this.relacionamentos(), servicoId);
  }

  protected ultimaAtualizacaoFormatada(): string {
    return formatarUltimaAtualizacao(this.ultimaAtualizacao());
  }

  protected voltarParaHome(): void {
    this.router.navigateByUrl('/');
  }

  private abrirDrawer(): void {
    this.limparFeedback();
    this.drawerAberto.set(true);
  }

  private salvarContrato(): void {
    if (!this.drawerFormValido()) {
      this.erro.set(MENSAGEM_OPERACAO_INVALIDA);
      return;
    }

    const payload = criarPayloadContrato(this.contratoForm);
    const request = this.contratoForm.id == null
      ? this.api.criarContrato(payload)
      : this.api.atualizarContrato(this.contratoForm.id, payload);

    this.executarAcao(
      request,
      this.mensagemSucessoPorModo(this.contratoForm.id),
      true
    );
  }

  private salvarServico(): void {
    if (!this.drawerFormValido()) {
      this.erro.set(MENSAGEM_OPERACAO_INVALIDA);
      return;
    }

    const payload = criarPayloadServico(this.servicoForm);
    const request = this.servicoForm.id == null
      ? this.api.criarServico(payload)
      : this.api.atualizarServico(this.servicoForm.id, payload);

    this.executarAcao(
      request,
      this.mensagemSucessoPorModo(this.servicoForm.id),
      true
    );
  }

  private salvarRelacionamento(): void {
    if (!this.drawerFormValido()) {
      this.erro.set(this.relacionamentoDuplicado() ? MENSAGEM_RELACIONAMENTO_DUPLICADO : MENSAGEM_OPERACAO_INVALIDA);
      return;
    }

    const payload = criarPayloadRelacionamento(this.relacionamentoForm);
    const request = this.relacionamentoForm.id == null
      ? this.api.criarRelacionamento(payload)
      : this.api.atualizarRelacionamento(this.relacionamentoForm.id, payload);

    this.executarAcao(
      request,
      this.mensagemSucessoPorModo(this.relacionamentoForm.id),
      true
    );
  }

  private executarAcao<T>(request: Observable<T>, mensagemSucesso: string, fecharDrawer = false): void {
    this.salvando.set(true);
    this.limparFeedback();

    request
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: () => {
          this.mensagem.set(mensagemSucesso);
          if (fecharDrawer) {
            this.fecharDrawer();
          }
          this.carregarBaseAdministrativa(true);
        },
        error: (error: unknown) => {
          this.erro.set(this.apiFeedback.mensagem(
            error as { status?: number; error?: { mensagem?: string } } | null | undefined,
            MENSAGEM_OPERACAO_INVALIDA
          ));
        }
      });
  }

  private carregarBaseAdministrativa(preservarFeedback = false): void {
    this.carregando.set(true);
    if (!preservarFeedback) {
      this.limparFeedback();
    }

    forkJoin({
      contratos: this.api.listarContratos(),
      servicos: this.api.listarServicos(),
      relacionamentos: this.api.listarRelacionamentos()
    })
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: ({ contratos, servicos, relacionamentos }) => {
          this.contratos.set(contratos.dados);
          this.servicos.set(servicos.dados);
          this.relacionamentos.set(relacionamentos.dados);
          this.ultimaAtualizacao.set(new Date());
        },
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, MENSAGEM_CARGA_ADMINISTRATIVA));
        }
      });
  }

  private executarAlternancia<T>(ativo: boolean, requestInativar: Observable<T>, requestAtivar: Observable<T>): void {
    this.executarAcao(
      ativo ? requestInativar : requestAtivar,
      ativo ? MENSAGEM_REGISTRO_INATIVADO : MENSAGEM_REGISTRO_ATIVADO
    );
  }

  private mensagemSucessoPorModo(id: number | null): string {
    return id == null ? MENSAGEM_REGISTRO_SALVO : MENSAGEM_ALTERACOES_SALVAS;
  }

  private relacionamentoDuplicado(): boolean {
    return relacionamentoDuplicado(this.relacionamentos(), this.relacionamentoForm);
  }

  private limparFeedback(): void {
    this.mensagem.set('');
    this.erro.set('');
  }
}
