import { SoftwareResumo } from './painel-operacional.models';

export type StatusSoftware = 'Disponível' | 'Pendente' | 'Indisponível';

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
    ? (possuiVersao ? 'Disponível' : 'Pendente')
    : 'Indisponível';

  return {
    ...item,
    descricaoSoftware: item.descricaoSoftware ?? '',
    versaoReferencia: item.versaoReferencia ?? '',
    linkSoftware: item.linkSoftware ?? '',
    status,
    ultimaAtualizacao,
    origemArquivo: item.artefatoNome?.trim() || 'Base operacional ATLAS',
    observacao: item.descricaoSoftware?.trim()
      ? item.descricaoSoftware
      : 'Arquivo utilizado na operação e consolidado a partir da base atual de serviços.',
    downloadDisponivel: Boolean(item.downloadDisponivel)
  };
}
