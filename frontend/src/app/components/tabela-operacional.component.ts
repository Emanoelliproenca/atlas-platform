import { Component, computed, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PainelLinha } from '../painel-operacional-api';
import { ActionIconComponent } from './action-icon.component';

@Component({
  selector: 'app-tabela-operacional',
  imports: [RouterLink, ActionIconComponent],
  templateUrl: './tabela-operacional.component.html',
  styleUrl: './tabela-operacional.component.scss'
})
export class TabelaOperacionalComponent {
  readonly linhas = input.required<PainelLinha[]>();
  readonly ultimaAtualizacao = input.required<string>();

  protected readonly tamanhoPagina = signal(10);
  protected readonly paginaAtual = signal(0);
  protected readonly opcoesTamanhoPagina = [5, 10, 25];

  protected readonly totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.linhas().length / this.tamanhoPagina()))
  );

  protected readonly paginaNormalizada = computed(() =>
    Math.min(this.paginaAtual(), this.totalPaginas() - 1)
  );

  protected readonly linhasPaginadas = computed(() => {
    const inicio = this.paginaNormalizada() * this.tamanhoPagina();
    return this.linhas().slice(inicio, inicio + this.tamanhoPagina());
  });

  protected readonly primeiroItem = computed(() =>
    this.linhas().length === 0 ? 0 : this.paginaNormalizada() * this.tamanhoPagina() + 1
  );

  protected readonly ultimoItem = computed(() =>
    Math.min((this.paginaNormalizada() + 1) * this.tamanhoPagina(), this.linhas().length)
  );

  protected paginaAnterior(): void {
    this.paginaAtual.update((pagina) => Math.max(0, pagina - 1));
  }

  protected proximaPagina(): void {
    this.paginaAtual.update((pagina) => Math.min(this.totalPaginas() - 1, pagina + 1));
  }

  protected alterarTamanhoPagina(valor: string): void {
    this.tamanhoPagina.set(Number(valor));
    this.paginaAtual.set(0);
  }
}
