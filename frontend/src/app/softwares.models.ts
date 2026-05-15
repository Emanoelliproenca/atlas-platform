import { SoftwareResumo } from './painel-operacional.models';

export type StatusSoftware = 'Disponivel' | 'Pendente' | 'Indisponivel';

export interface SoftwareViewModel extends SoftwareResumo {
  status: StatusSoftware;
  ultimaAtualizacao: string;
  origemArquivo: string;
  observacao: string;
  downloadDisponivel: boolean;
}

export function mapearSoftwareViewModel(
  item: SoftwareResumo,
  ultimaAtualizacao: string
): SoftwareViewModel {
  const possuiVersao = Boolean(item.versaoReferencia?.trim());
  const status: StatusSoftware = item.ativo
    ? (possuiVersao ? 'Disponivel' : 'Pendente')
    : 'Indisponivel';

  return {
    ...item,
    descricaoSoftware: item.descricaoSoftware ?? '',
    versaoReferencia: item.versaoReferencia ?? '',
    status,
    ultimaAtualizacao,
    origemArquivo: 'Repositorio demo ATLAS',
    observacao: item.descricaoSoftware?.trim()
      ? item.descricaoSoftware
      : 'Arquivo utilizado na operacao e consolidado a partir da base atual de servicos.',
    downloadDisponivel: status === 'Disponivel'
  };
}
