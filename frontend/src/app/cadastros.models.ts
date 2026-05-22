import { ContratoServicoResponse, ContratoResponse, ServicoResponse } from './painel-operacional.models';

export type AbaCadastro = 'contratos' | 'servicos' | 'relacionamentos';
export type DrawerEntidade = 'contrato' | 'servico' | 'relacionamento';
export type DrawerModo = 'novo' | 'editar' | 'visualizar';
export type FiltroStatus = 'todos' | 'ativos' | 'inativos';

export interface ContratoFormModel {
  id: number | null;
  nome: string;
  grupo: string;
  centralUrl: string;
  particularidades: string;
  documentacao: string;
  ativo: boolean;
}

export interface ServicoFormModel {
  id: number | null;
  nome: string;
  descricaoSoftware: string;
  versaoReferencia: string;
  linkSoftware: string;
  ativo: boolean;
}

export interface RelacionamentoFormModel {
  id: number | null;
  contratoId: number;
  servicoId: number;
  versao: string;
  setor: string;
  observacao: string;
  ativo: boolean;
}

export const ABAS_CADASTRO: Array<{ id: AbaCadastro; label: string; hint: string }> = [
  { id: 'contratos', label: 'Contratos', hint: 'Base operacional' },
  { id: 'servicos', label: 'Serviços', hint: 'Catálogo digital' },
  { id: 'relacionamentos', label: 'Relacionamentos', hint: 'Matriz operacional' }
];

export const DRAWER_ENTITY_LABELS: Record<DrawerEntidade, string> = {
  contrato: 'contrato',
  servico: 'serviço',
  relacionamento: 'relacionamento'
};

export const DRAWER_SECTION_LABELS: Record<DrawerEntidade, string> = {
  contrato: 'Contratos',
  servico: 'Serviços',
  relacionamento: 'Relacionamentos'
};

export function criarContratoVazio(): ContratoFormModel {
  return {
    id: null,
    nome: '',
    grupo: '',
    centralUrl: '',
    particularidades: '',
    documentacao: '',
    ativo: true
  };
}

export function criarServicoVazio(): ServicoFormModel {
  return {
    id: null,
    nome: '',
    descricaoSoftware: '',
    versaoReferencia: '',
    linkSoftware: '',
    ativo: true
  };
}

export function criarRelacionamentoVazio(): RelacionamentoFormModel {
  return {
    id: null,
    contratoId: 0,
    servicoId: 0,
    versao: '',
    setor: '',
    observacao: '',
    ativo: true
  };
}

export function mapearContratoParaForm(item?: ContratoResponse): ContratoFormModel {
  if (!item) {
    return criarContratoVazio();
  }

  return {
    id: item.id,
    nome: item.nome,
    grupo: item.grupo,
    centralUrl: item.centralUrl,
    particularidades: item.particularidades ?? '',
    documentacao: item.documentacao ?? '',
    ativo: item.ativo
  };
}

export function mapearServicoParaForm(item?: ServicoResponse): ServicoFormModel {
  if (!item) {
    return criarServicoVazio();
  }

  return {
    id: item.id,
    nome: item.nome,
    descricaoSoftware: item.descricaoSoftware ?? '',
    versaoReferencia: item.versaoReferencia ?? '',
    linkSoftware: item.linkSoftware ?? '',
    ativo: item.ativo
  };
}

export function mapearRelacionamentoParaForm(item?: ContratoServicoResponse): RelacionamentoFormModel {
  if (!item) {
    return criarRelacionamentoVazio();
  }

  return {
    id: item.id,
    contratoId: item.contratoId,
    servicoId: item.servicoId,
    versao: item.versao ?? '',
    setor: item.setor ?? '',
    observacao: item.observacao ?? '',
    ativo: item.ativo
  };
}

export function filtrarPorStatus(ativo: boolean, filtro: FiltroStatus): boolean {
  if (filtro === 'todos') {
    return true;
  }

  return filtro === 'ativos' ? ativo : !ativo;
}

export function contemTexto(valor: string, busca: string): boolean {
  return !busca || valor.toLowerCase().includes(busca.toLowerCase());
}

export function resumirObservacao(valor: string): string {
  if (!valor) {
    return '-';
  }

  return valor.length > 56 ? `${valor.slice(0, 56)}...` : valor;
}
