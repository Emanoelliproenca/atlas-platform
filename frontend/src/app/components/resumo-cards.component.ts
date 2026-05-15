import { Component, input } from '@angular/core';
import { PainelResumo } from '../painel-operacional-api';

@Component({
  selector: 'app-resumo-cards',
  templateUrl: './resumo-cards.component.html',
  styleUrl: './resumo-cards.component.scss'
})
export class ResumoCardsComponent {
  readonly resumo = input.required<PainelResumo>();
}
