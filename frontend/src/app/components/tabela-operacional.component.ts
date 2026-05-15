import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PainelLinha } from '../painel-operacional-api';

@Component({
  selector: 'app-tabela-operacional',
  imports: [RouterLink],
  templateUrl: './tabela-operacional.component.html',
  styleUrl: './tabela-operacional.component.scss'
})
export class TabelaOperacionalComponent {
  readonly linhas = input.required<PainelLinha[]>();
  readonly ultimaAtualizacao = input.required<string>();
}
