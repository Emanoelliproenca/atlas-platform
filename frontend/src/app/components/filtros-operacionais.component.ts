import { Component, computed, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { PainelFiltros } from '../painel-operacional-api';

@Component({
  selector: 'app-filtros-operacionais',
  imports: [FormsModule, MatFormFieldModule, MatSelectModule],
  templateUrl: './filtros-operacionais.component.html',
  styleUrl: './filtros-operacionais.component.scss'
})
export class FiltrosOperacionaisComponent {
  readonly filtros = input.required<PainelFiltros>();
  readonly grupo = input.required<string>();
  readonly setor = input.required<string>();
  readonly contratoIds = input.required<number[]>();
  readonly servicoIds = input.required<number[]>();
  readonly carregando = input.required<boolean>();

  protected readonly contratoSelecionado = computed(() => this.contratoIds()[0] ?? 0);
  protected readonly servicoSelecionado = computed(() => this.servicoIds()[0] ?? 0);

  readonly grupoChange = output<string>();
  readonly setorChange = output<string>();
  readonly contratosSelecionadosChange = output<number[]>();
  readonly servicosSelecionadosChange = output<number[]>();
  readonly aplicar = output<void>();
  readonly limpar = output<void>();

  protected selecionarContrato(contratoId: number): void {
    this.contratosSelecionadosChange.emit(contratoId ? [contratoId] : []);
  }

  protected selecionarServico(servicoId: number): void {
    this.servicosSelecionadosChange.emit(servicoId ? [servicoId] : []);
  }
}
