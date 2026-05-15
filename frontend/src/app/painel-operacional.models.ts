export interface ApiResponse<T> {
  sucesso: boolean;
  mensagem: string;
  dados: T;
}

export interface ContratoOption {
  id: number;
  nome: string;
  grupo: string;
  centralUrl: string;
  ativo: boolean;
}

export interface ServicoOption {
  id: number;
  nome: string;
  descricaoSoftware: string;
  ativo: boolean;
}

export interface PainelResumo {
  totalRelacionamentos: number;
  totalContratos: number;
  totalServicos: number;
  totalGrupos: number;
  totalSetores: number;
}

export interface PainelFiltros {
  contratos: ContratoOption[];
  servicos: ServicoOption[];
  grupos: string[];
  setores: string[];
}

export interface PainelLinha {
  relacionamentoId: number;
  contratoId: number;
  contratoNome: string;
  grupo: string;
  centralUrl: string;
  servicoId: number;
  servicoNome: string;
  versao: string;
  observacao: string;
  setor: string;
}

export interface PainelOperacional {
  resumo: PainelResumo;
  filtros: PainelFiltros;
  linhas: PainelLinha[];
}

export interface PainelRequest {
  contratoIds: number[];
  servicoIds: number[];
  grupo: string;
  setor: string;
  contratoNome: string;
  servicoNome: string;
}

export interface ContratoResponse {
  id: number;
  nome: string;
  grupo: string;
  centralUrl: string;
  particularidades: string;
  documentacao: string;
  ativo: boolean;
}

export interface ServicoResponse {
  id: number;
  nome: string;
  descricaoSoftware: string;
  ativo: boolean;
}

export interface ContratoServicoResponse {
  id: number;
  contratoId: number;
  contratoNome: string;
  servicoId: number;
  servicoNome: string;
  versao: string;
  observacao: string;
  setor: string;
  ativo: boolean;
}

export interface ContratoArquivoResponse {
  id: number;
  nome: string;
  tipoConteudo: string;
  tamanho: number;
  criadoEm: string;
}

export interface ContratoDetalhe {
  contrato: ContratoResponse;
  servicos: ContratoServicoResponse[];
  arquivos?: ContratoArquivoResponse[];
}

export interface ServicoDetalhe {
  servico: ServicoResponse;
  contratos: ContratoServicoResponse[];
}

export interface SessaoUsuario {
  username: string;
  roles: string[];
  expiraEm: string | null;
}

export interface AuthLoginPayload {
  username: string;
  password: string;
}

export interface AuthLoginResult {
  token: string;
  username: string;
  roles: string[];
  expiraEm: string | null;
}

export interface RepasseItem {
  id: number;
  titulo: string;
  categoria: string;
  conteudo: string;
  prioridade: string;
  ativo: boolean;
}

export interface SoftwareResumo {
  id: number;
  nome: string;
  descricaoSoftware: string;
  versaoReferencia: string;
  totalContratos: number;
  ativo: boolean;
}

export interface ContratoServicoRequestPayload {
  contratoId: number;
  servicoId: number;
  versao: string;
  observacao: string;
  setor: string;
  ativo: boolean;
}

export interface ContratoRequestPayload {
  nome: string;
  grupo: string;
  centralUrl: string;
  particularidades: string;
  documentacao: string;
  ativo: boolean;
}

export interface ServicoRequestPayload {
  nome: string;
  descricaoSoftware: string;
  ativo: boolean;
}
