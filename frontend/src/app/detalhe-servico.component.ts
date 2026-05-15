import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiFeedbackService } from './api-feedback.service';
import { MetricStripComponent } from './components/metric-strip.component';
import { PageHeaderComponent } from './components/page-header.component';
import {
  criarMetricasRelacionamento,
  resumoQuantidadeRelacionamentos,
  totalSetoresRelacionados,
  ultimaVersaoRelacionada
} from './detalhe-operacional.utils';
import { PainelOperacionalApi, ServicoDetalhe } from './painel-operacional-api';

@Component({
  selector: 'app-detalhe-servico',
  imports: [CommonModule, RouterLink, PageHeaderComponent, MetricStripComponent],
  templateUrl: './detalhe-servico.component.html',
  styleUrl: './detalhe-servico.component.scss'
})
export class DetalheServicoComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(PainelOperacionalApi);
  private readonly apiFeedback = inject(ApiFeedbackService);

  protected readonly detalhe = signal<ServicoDetalhe | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal('');
  constructor() {
    const servicoId = Number(this.route.snapshot.paramMap.get('id'));

    if (!servicoId) {
      this.carregando.set(false);
      this.erro.set('Servico invalido.');
      return;
    }

    this.api.carregarServicoDetalhe(servicoId)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (response) => this.detalhe.set(response.dados),
        error: (error) => {
          this.erro.set(this.apiFeedback.mensagem(error, 'Nao foi possivel carregar o detalhe do servico.'));
        }
      });
  }

  protected resumoRelacionamento(totalContratos: number): string {
    return resumoQuantidadeRelacionamentos('contrato', 'contratos', totalContratos);
  }

  protected totalSetores(dados: ServicoDetalhe): number {
    return totalSetoresRelacionados(dados.contratos);
  }

  protected ultimaVersao(dados: ServicoDetalhe): string {
    return ultimaVersaoRelacionada(dados.contratos);
  }

  protected metricas(dados: ServicoDetalhe) {
    return criarMetricasRelacionamento(
      'Contratos vinculados',
      dados.contratos.length,
      'Setores atendidos',
      this.totalSetores(dados),
      this.ultimaVersao(dados)
    );
  }
}
