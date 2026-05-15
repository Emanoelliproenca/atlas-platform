import { CommonModule, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiFeedbackService } from './api-feedback.service';
import { AuthSessionService } from './auth-session.service';
import { PainelOperacionalApi, RepasseItem } from './painel-operacional-api';

type RepasseCategoria = 'Todos' | 'Urgente' | 'Informativo' | 'Atualizacao' | 'Manutencao';

interface RepasseViewModel extends RepasseItem {
  autor: string;
  criadoEm: string | null;
  fixado: boolean;
  anexoNome: string | null;
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

const FILTROS: RepasseCategoria[] = ['Todos', 'Urgente', 'Informativo', 'Atualizacao', 'Manutencao'];

const EMPTY_FORM: RepasseFormModel = {
  id: null,
  titulo: '',
  conteudo: '',
  categoria: 'Informativo',
  prioridade: 'Media',
  fixado: false,
  anexoNome: ''
};

@Component({
  selector: 'app-repasse',
  imports: [CommonModule, FormsModule, DatePipe],
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
  protected readonly ehAdmin = computed(() => this.authSession.session()?.roles.includes('ROLE_ADMIN') ?? false);
  protected readonly modalAberto = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected readonly filtroAtual = signal<RepasseCategoria>('Todos');
  protected readonly formulario = signal<RepasseFormModel>({ ...EMPTY_FORM });
  protected readonly filtros = FILTROS;

  protected readonly itensFiltrados = computed(() => {
    const filtro = this.filtroAtual();
    const itens = this.itens();

    if (filtro === 'Todos') {
      return itens;
    }

    return itens.filter((item) => item.categoriaFiltro === filtro);
  });

  protected readonly repasseFixado = computed(() => this.itensFiltrados().find((item) => item.fixado) ?? null);
  protected readonly listaRepasses = computed(() =>
    this.itensFiltrados().filter((item) => !this.repasseFixado() || item.id !== this.repasseFixado()!.id)
  );

  constructor() {
    this.api.carregarRepasses()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (response) => {
          this.itens.set(this.mapearItens(response.dados));
        },
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, 'Nao foi possivel carregar os repasses.'));
        }
      });
  }

  protected selecionarFiltro(filtro: RepasseCategoria): void {
    this.filtroAtual.set(filtro);
  }

  protected abrirNovoRepasse(): void {
    this.formulario.set({ ...EMPTY_FORM });
    this.editandoId.set(null);
    this.modalAberto.set(true);
  }

  protected editarRepasse(item: RepasseViewModel): void {
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
    this.itens.set(this.itens().filter((item) => item.id !== itemId));
  }

  protected alternarFixado(item: RepasseViewModel): void {
    this.itens.update((itens) =>
      this.ordenarRepasses(
        itens.map((atual) => ({
          ...atual,
          fixado: atual.id === item.id ? !atual.fixado : false
        }))
      )
    );
  }

  protected abrirAnexo(item: RepasseViewModel): void {
    if (!item.anexoNome) {
      return;
    }

    window.alert(`Anexo disponivel: ${item.anexoNome}`);
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
    if (!formulario.titulo.trim() || !formulario.conteudo.trim()) {
      return;
    }

    const agora = new Date().toISOString();
    const autor = this.authSession.session()?.username ?? 'ATLAS';

    if (this.editandoId() != null) {
      this.itens.update((itens) =>
        this.ordenarRepasses(
          itens.map((item) =>
            item.id === this.editandoId()
              ? {
                  ...item,
                  titulo: formulario.titulo.trim(),
                  conteudo: formulario.conteudo.trim(),
                  categoria: formulario.categoria,
                  categoriaFiltro: formulario.categoria,
                  prioridade: formulario.prioridade,
                  fixado: formulario.fixado,
                  anexoNome: formulario.anexoNome.trim() || null,
                  autor,
                  criadoEm: agora
                }
              : {
                  ...item,
                  fixado: formulario.fixado ? false : item.fixado
                }
          )
        )
      );
    } else {
      const novoRepasse: RepasseViewModel = {
        id: this.proximoId(),
        titulo: formulario.titulo.trim(),
        conteudo: formulario.conteudo.trim(),
        categoria: formulario.categoria,
        categoriaFiltro: formulario.categoria,
        prioridade: formulario.prioridade,
        ativo: true,
        autor,
        criadoEm: agora,
        fixado: formulario.fixado,
        anexoNome: formulario.anexoNome.trim() || null
      };

      this.itens.update((itens) =>
        this.ordenarRepasses([
          ...itens.map((item) => ({
            ...item,
            fixado: formulario.fixado ? false : item.fixado
          })),
          novoRepasse
        ])
      );
    }

    this.fecharModal();
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
      itens.map((item, index) => ({
        ...item,
        categoriaFiltro: this.normalizarCategoria(item.categoria),
        autor: 'ATLAS',
        criadoEm: new Date(Date.now() - index * 60 * 60 * 1000).toISOString(),
        fixado: index === 0,
        anexoNome: index === 0 ? 'procedimento-atualizado.pdf' : null
      }))
    );
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
      return 'Manutencao';
    }

    if (normalizada.includes('atual')) {
      return 'Atualizacao';
    }

    return 'Informativo';
  }

  private proximoId(): number {
    return this.itens().reduce((maiorId, item) => Math.max(maiorId, item.id), 0) + 1;
  }
}
