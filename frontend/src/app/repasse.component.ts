import { CommonModule, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { finalize } from 'rxjs';
import { ApiFeedbackService } from './api-feedback.service';
import { AuthSessionService } from './auth-session.service';
import { PainelOperacionalApi, RepasseItem, RepasseRequestPayload } from './painel-operacional-api';

type RepasseCategoria = 'Todos' | 'Urgente' | 'Informativo' | 'Atualização' | 'Manutenção';

interface RepasseViewModel extends Omit<RepasseItem, 'autor'> {
  autor: string | null;
  categoriaFiltro: Exclude<RepasseCategoria, 'Todos'>;
}

interface RepasseFormModel {
  id: number | null;
  titulo: string;
  conteudo: string;
  categoria: Exclude<RepasseCategoria, 'Todos'>;
  prioridade: string;
  fixado: boolean;
  anexoNome: string;
}

const FILTROS: RepasseCategoria[] = ['Todos', 'Urgente', 'Informativo', 'Atualização', 'Manutenção'];
const EMPTY_FORM: RepasseFormModel = {
  id: null,
  titulo: '',
  conteudo: '',
  categoria: 'Informativo',
  prioridade: 'Média',
  fixado: false,
  anexoNome: ''
};

@Component({
  selector: 'app-repasse',
  imports: [CommonModule, FormsModule, DatePipe, MatFormFieldModule, MatSelectModule],
  templateUrl: './repasse.component.html',
  styleUrl: './repasse.component.scss'
})
export class RepasseComponent {
  private readonly api = inject(PainelOperacionalApi);
  private readonly apiFeedback = inject(ApiFeedbackService);
  private readonly authSession = inject(AuthSessionService);

  protected readonly itens = signal<RepasseViewModel[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal('');
  protected readonly mensagem = signal('');
  protected readonly ehAdmin = computed(() => this.authSession.session()?.roles.includes('ROLE_ADMIN') ?? false);
  protected readonly modalAberto = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected readonly filtroAtual = signal<RepasseCategoria>('Todos');
  protected readonly formulario = signal<RepasseFormModel>({ ...EMPTY_FORM });
  protected readonly filtros = FILTROS;
  protected readonly itensVisuais = computed(() => this.itens());

  protected readonly itensFiltrados = computed(() => {
    const filtro = this.filtroAtual();
    const itens = this.itensVisuais();

    if (filtro === 'Todos') {
      return itens;
    }

    return itens.filter((item) => item.categoriaFiltro === filtro);
  });

  protected readonly repasseFixado = computed(() => this.itensFiltrados().find((item) => item.fixado) ?? null);
  protected readonly listaRepasses = computed(() =>
    this.itensFiltrados().filter((item) => !this.repasseFixado() || item.id !== this.repasseFixado()!.id)
  );
  protected readonly formularioValido = computed(() =>
    Boolean(this.formulario().titulo.trim() && this.formulario().conteudo.trim())
  );

  constructor() {
    this.carregarRepasses();
  }

  private carregarRepasses(): void {
    this.carregando.set(true);
    this.api.carregarRepasses()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (response) => {
          this.itens.set(this.mapearItens(response.dados));
        },
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, 'Não foi possível carregar os repasses.'));
        }
      });
  }

  protected selecionarFiltro(filtro: RepasseCategoria): void {
    this.filtroAtual.set(filtro);
  }

  protected abrirNovoRepasse(): void {
    this.limparFeedback();
    this.formulario.set({ ...EMPTY_FORM });
    this.editandoId.set(null);
    this.modalAberto.set(true);
  }

  protected editarRepasse(item: RepasseViewModel): void {
    this.limparFeedback();
    this.formulario.set({
      id: item.id,
      titulo: item.titulo,
      conteudo: item.conteudo,
      categoria: this.normalizarCategoria(item.categoria),
      prioridade: item.prioridade,
      fixado: item.fixado,
      anexoNome: item.anexoNome ?? ''
    });
    this.editandoId.set(item.id);
    this.modalAberto.set(true);
  }

  protected excluirRepasse(itemId: number): void {
    this.limparFeedback();
    this.api.inativarRepasse(itemId).subscribe({
      next: () => {
        this.mensagem.set('Repasse inativado com sucesso.');
        this.carregarRepasses();
      },
      error: (error) => {
        this.erro.set(this.apiFeedback.mensagem(error, 'Não foi possível inativar o repasse.'));
      }
    });
  }

  protected alternarFixado(item: RepasseViewModel): void {
    this.limparFeedback();
    this.api.fixarRepasse(item.id).subscribe({
      next: () => {
        this.mensagem.set(item.fixado ? 'Repasse removido do destaque.' : 'Repasse fixado no topo.');
        this.carregarRepasses();
      },
      error: (error) => {
        this.erro.set(this.apiFeedback.mensagem(error, 'Não foi possível atualizar o destaque do repasse.'));
      }
    });
  }

  protected abrirAnexo(item: RepasseViewModel): void {
    if (!item.anexoNome) {
      return;
    }

    this.mensagem.set(`Anexo disponível para consulta: ${item.anexoNome}`);
    this.erro.set('');
  }

  protected fecharModal(): void {
    this.modalAberto.set(false);
    this.formulario.set({ ...EMPTY_FORM });
    this.editandoId.set(null);
  }

  protected atualizarFormulario<K extends keyof RepasseFormModel>(campo: K, valor: RepasseFormModel[K]): void {
    this.formulario.update((formulario) => ({
      ...formulario,
      [campo]: valor
    }));
  }

  protected salvarRepasse(): void {
    const formulario = this.formulario();
    if (!this.formularioValido()) {
      this.erro.set('Informe título e descrição para salvar o repasse.');
      return;
    }

    const editando = this.editandoId() != null;
    const payload = this.criarPayloadRepasse(formulario);
    const request = editando && formulario.id != null
      ? this.api.atualizarRepasse(formulario.id, payload)
      : this.api.criarRepasse(payload);

    this.limparFeedback();
    request.subscribe({
      next: () => {
        this.fecharModal();
        this.mensagem.set(editando ? 'Repasse atualizado com sucesso.' : 'Repasse criado com sucesso.');
        this.carregarRepasses();
      },
      error: (error) => {
        this.erro.set(this.apiFeedback.mensagem(error, 'Não foi possível salvar o repasse.'));
      }
    });
  }

  protected trackById(_: number, item: RepasseViewModel): number {
    return item.id;
  }

  protected tagLabel(item: RepasseViewModel): string {
    if (item.fixado) {
      return 'Fixado';
    }

    return item.categoria;
  }

  private mapearItens(itens: RepasseItem[]): RepasseViewModel[] {
    return this.ordenarRepasses(
      itens.map((item) => ({
        ...item,
        categoriaFiltro: this.normalizarCategoria(item.categoria),
        autor: item.autor ?? 'ATLAS',
        criadoEm: item.criadoEm ?? null,
        anexoNome: item.anexoNome ?? null,
        fixado: Boolean(item.fixado)
      }))
    );
  }

  private criarPayloadRepasse(formulario: RepasseFormModel): RepasseRequestPayload {
    return {
      titulo: formulario.titulo.trim(),
      conteudo: formulario.conteudo.trim(),
      categoria: formulario.categoria,
      prioridade: formulario.prioridade,
      ativo: true,
      fixado: formulario.fixado,
      anexoNome: formulario.anexoNome.trim()
    };
  }

  private ordenarRepasses(itens: RepasseViewModel[]): RepasseViewModel[] {
    return [...itens].sort((a, b) => {
      if (a.fixado !== b.fixado) {
        return a.fixado ? -1 : 1;
      }

      if (a.criadoEm && b.criadoEm) {
        return new Date(b.criadoEm).getTime() - new Date(a.criadoEm).getTime();
      }

      return b.id - a.id;
    });
  }

  private normalizarCategoria(categoria: string): Exclude<RepasseCategoria, 'Todos'> {
    const normalizada = categoria.trim().toLowerCase();

    if (normalizada.includes('urg')) {
      return 'Urgente';
    }

    if (normalizada.includes('manut')) {
      return 'Manutenção';
    }

    if (normalizada.includes('atual')) {
      return 'Atualização';
    }

    return 'Informativo';
  }

  private limparFeedback(): void {
    this.erro.set('');
    this.mensagem.set('');
  }
}
