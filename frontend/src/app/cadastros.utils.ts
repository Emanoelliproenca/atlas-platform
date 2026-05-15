import {
  ContratoServicoRequestPayload,
  ContratoServicoResponse,
  ContratoRequestPayload,
  ContratoResponse,
  ServicoRequestPayload,
  ServicoResponse
} from './painel-operacional.models';
import { ContratoFormModel, FiltroStatus, RelacionamentoFormModel, ServicoFormModel, contemTexto, filtrarPorStatus } from './cadastros.models';

export function gruposOrdenados(contratos: ContratoResponse[]): string[] {
  return [...new Set(contratos.map((item) => item.grupo).filter(Boolean))].sort((a, b) => a.localeCompare(b));
}

export function setoresOrdenados(relacionamentos: ContratoServicoResponse[]): string[] {
  return [...new Set(relacionamentos.map((item) => item.setor).filter(Boolean))].sort((a, b) => a.localeCompare(b));
}

export function contarInativos(
  contratos: ContratoResponse[],
  servicos: ServicoResponse[],
  relacionamentos: ContratoServicoResponse[]
): number {
  return [
    ...contratos.map((item) => item.ativo),
    ...servicos.map((item) => item.ativo),
    ...relacionamentos.map((item) => item.ativo)
  ].filter((ativo) => !ativo).length;
}

export function filtrarContratos(
  contratos: ContratoResponse[],
  nome: string,
  grupo: string,
  status: FiltroStatus
): ContratoResponse[] {
  return contratos.filter((item) =>
    filtrarPorStatus(item.ativo, status) &&
    contemTexto(item.nome, nome) &&
    (!grupo || item.grupo === grupo)
  );
}

export function filtrarServicos(
  servicos: ServicoResponse[],
  nome: string,
  status: FiltroStatus
): ServicoResponse[] {
  return servicos.filter((item) =>
    filtrarPorStatus(item.ativo, status) &&
    contemTexto(item.nome, nome)
  );
}

export function filtrarRelacionamentos(
  relacionamentos: ContratoServicoResponse[],
  contratoId: number,
  servicoId: number,
  setor: string,
  status: FiltroStatus
): ContratoServicoResponse[] {
  return relacionamentos.filter((item) =>
    filtrarPorStatus(item.ativo, status) &&
    (!contratoId || item.contratoId === contratoId) &&
    (!servicoId || item.servicoId === servicoId) &&
    (!setor || item.setor === setor)
  );
}

export function contarContratosAtivosPorServico(relacionamentos: ContratoServicoResponse[], servicoId: number): number {
  return relacionamentos.filter((item) => item.servicoId === servicoId && item.ativo).length;
}

export function relacionamentoDuplicado(
  relacionamentos: ContratoServicoResponse[],
  form: RelacionamentoFormModel
): boolean {
  return relacionamentos.some((item) =>
    item.contratoId === form.contratoId &&
    item.servicoId === form.servicoId &&
    item.id !== form.id
  );
}

export function formatarUltimaAtualizacao(data: Date): string {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short'
  }).format(data);
}

export function criarPayloadContrato(form: ContratoFormModel): ContratoRequestPayload {
  return {
    nome: form.nome.trim(),
    grupo: form.grupo.trim(),
    centralUrl: form.centralUrl.trim(),
    particularidades: form.particularidades.trim(),
    documentacao: form.documentacao.trim(),
    ativo: form.ativo
  };
}

export function criarPayloadServico(form: ServicoFormModel): ServicoRequestPayload {
  return {
    nome: form.nome.trim(),
    descricaoSoftware: form.descricaoSoftware.trim(),
    ativo: form.ativo
  };
}

export function criarPayloadRelacionamento(form: RelacionamentoFormModel): ContratoServicoRequestPayload {
  return {
    contratoId: form.contratoId,
    servicoId: form.servicoId,
    versao: form.versao.trim(),
    observacao: form.observacao.trim(),
    setor: form.setor.trim(),
    ativo: form.ativo
  };
}
