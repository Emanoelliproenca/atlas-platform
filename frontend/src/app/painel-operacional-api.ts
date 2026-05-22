import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  AuthLoginPayload,
  AuthLoginResult,
  ContratoArquivoResponse,
  ContratoDetalhe,
  ContratoRequestPayload,
  ContratoResponse,
  ContratoServicoRequestPayload,
  ContratoServicoResponse,
  DashboardMetrics,
  PainelOperacional,
  PainelRequest,
  RepasseItem,
  RepasseRequestPayload,
  ServicoDetalhe,
  ServicoRequestPayload,
  ServicoResponse,
  SessaoUsuario,
  SoftwareResumo
} from './painel-operacional.models';

@Injectable({ providedIn: 'root' })
export class PainelOperacionalApi {
  private readonly http = inject(HttpClient);

  carregarPainel(request: PainelRequest): Observable<ApiResponse<PainelOperacional>> {
    return this.get('/painel/operacional', this.buildPainelParams(request));
  }

  carregarDashboardMetrics(): Observable<ApiResponse<DashboardMetrics>> {
    return this.get('/dashboard/metrics');
  }

  carregarContratoDetalhe(contratoId: number): Observable<ApiResponse<ContratoDetalhe>> {
    return this.get(`/contratos/${contratoId}/detalhe`);
  }

  uploadContratoArquivo(contratoId: number, arquivo: File): Observable<ApiResponse<ContratoArquivoResponse>> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    return this.post(`/contratos/${contratoId}/arquivos`, formData);
  }

  abrirContratoArquivo(contratoId: number, arquivoId: number): Observable<Blob> {
    return this.getBlob(`/contratos/${contratoId}/arquivos/${arquivoId}/abrir`);
  }

  baixarContratoArquivo(contratoId: number, arquivoId: number): Observable<Blob> {
    return this.getBlob(`/contratos/${contratoId}/arquivos/${arquivoId}/baixar`);
  }

  carregarServicoDetalhe(servicoId: number): Observable<ApiResponse<ServicoDetalhe>> {
    return this.get(`/servicos/${servicoId}/detalhe`);
  }

  carregarSessao(): Observable<ApiResponse<SessaoUsuario>> {
    return this.get('/auth/me');
  }

  login(request: AuthLoginPayload): Observable<ApiResponse<AuthLoginResult>> {
    return this.post('/auth/login', request);
  }

  logout(): Observable<ApiResponse<void>> {
    return this.post('/auth/logout', {});
  }

  atualizarContrato(
    contratoId: number,
    request: Omit<ContratoResponse, 'id'>
  ): Observable<ApiResponse<ContratoResponse>> {
    return this.put(`/contratos/${contratoId}`, request);
  }

  listarContratos(): Observable<ApiResponse<ContratoResponse[]>> {
    return this.get('/contratos');
  }

  criarContrato(request: ContratoRequestPayload): Observable<ApiResponse<ContratoResponse>> {
    return this.post('/contratos', request);
  }

  ativarContrato(contratoId: number): Observable<ApiResponse<ContratoResponse>> {
    return this.patch(`/contratos/${contratoId}/ativar`);
  }

  inativarContrato(contratoId: number): Observable<ApiResponse<ContratoResponse>> {
    return this.patch(`/contratos/${contratoId}/inativar`);
  }

  listarServicos(): Observable<ApiResponse<ServicoResponse[]>> {
    return this.get('/servicos');
  }

  criarServico(request: ServicoRequestPayload): Observable<ApiResponse<ServicoResponse>> {
    return this.post('/servicos', request);
  }

  ativarServico(servicoId: number): Observable<ApiResponse<ServicoResponse>> {
    return this.patch(`/servicos/${servicoId}/ativar`);
  }

  inativarServico(servicoId: number): Observable<ApiResponse<ServicoResponse>> {
    return this.patch(`/servicos/${servicoId}/inativar`);
  }

  listarRelacionamentos(): Observable<ApiResponse<ContratoServicoResponse[]>> {
    return this.get('/contrato-servicos');
  }

  criarRelacionamento(request: ContratoServicoRequestPayload): Observable<ApiResponse<ContratoServicoResponse>> {
    return this.post('/contrato-servicos', request);
  }

  atualizarRelacionamento(
    relacionamentoId: number,
    request: ContratoServicoRequestPayload
  ): Observable<ApiResponse<ContratoServicoResponse>> {
    return this.put(`/contrato-servicos/${relacionamentoId}`, request);
  }

  ativarRelacionamento(relacionamentoId: number): Observable<ApiResponse<ContratoServicoResponse>> {
    return this.patch(`/contrato-servicos/${relacionamentoId}/ativar`);
  }

  inativarRelacionamento(relacionamentoId: number): Observable<ApiResponse<ContratoServicoResponse>> {
    return this.patch(`/contrato-servicos/${relacionamentoId}/inativar`);
  }

  atualizarServico(servicoId: number, request: Omit<ServicoResponse, 'id'>): Observable<ApiResponse<ServicoResponse>> {
    return this.put(`/servicos/${servicoId}`, request);
  }

  deletarRelacionamento(relacionamentoId: number): Observable<ApiResponse<void>> {
    return this.delete(`/contrato-servicos/${relacionamentoId}`);
  }

  carregarRepasses(): Observable<ApiResponse<RepasseItem[]>> {
    return this.get('/repasses');
  }

  criarRepasse(request: RepasseRequestPayload): Observable<ApiResponse<RepasseItem>> {
    return this.post('/repasses', request);
  }

  atualizarRepasse(repasseId: number, request: RepasseRequestPayload): Observable<ApiResponse<RepasseItem>> {
    return this.put(`/repasses/${repasseId}`, request);
  }

  ativarRepasse(repasseId: number): Observable<ApiResponse<RepasseItem>> {
    return this.patch(`/repasses/${repasseId}/ativar`);
  }

  inativarRepasse(repasseId: number): Observable<ApiResponse<RepasseItem>> {
    return this.patch(`/repasses/${repasseId}/inativar`);
  }

  fixarRepasse(repasseId: number): Observable<ApiResponse<RepasseItem>> {
    return this.patch(`/repasses/${repasseId}/fixar`);
  }

  carregarSoftwares(): Observable<ApiResponse<SoftwareResumo[]>> {
    return this.get('/softwares');
  }

  criarSoftware(request: ServicoRequestPayload): Observable<ApiResponse<ServicoResponse>> {
    return this.post('/softwares', request);
  }

  atualizarSoftware(servicoId: number, request: ServicoRequestPayload): Observable<ApiResponse<ServicoResponse>> {
    return this.put(`/softwares/${servicoId}`, request);
  }

  ativarSoftware(servicoId: number): Observable<ApiResponse<ServicoResponse>> {
    return this.patch(`/softwares/${servicoId}/ativar`);
  }

  inativarSoftware(servicoId: number): Observable<ApiResponse<ServicoResponse>> {
    return this.patch(`/softwares/${servicoId}/inativar`);
  }

  baixarSoftware(servicoId: number): Observable<Blob> {
    return this.getBlob(`/softwares/${servicoId}/download`);
  }

  private get<T>(url: string, params?: HttpParams): Observable<ApiResponse<T>> {
    return this.http.get<ApiResponse<T>>(this.resolveApiUrl(url), params ? { params } : undefined);
  }

  private getBlob(url: string): Observable<Blob> {
    return this.http.get(this.resolveApiUrl(url), { responseType: 'blob' });
  }

  private post<T>(url: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http.post<ApiResponse<T>>(this.resolveApiUrl(url), body);
  }

  private put<T>(url: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http.put<ApiResponse<T>>(this.resolveApiUrl(url), body);
  }

  private patch<T>(url: string): Observable<ApiResponse<T>> {
    return this.http.patch<ApiResponse<T>>(this.resolveApiUrl(url), {});
  }

  private delete<T>(url: string): Observable<ApiResponse<T>> {
    return this.http.delete<ApiResponse<T>>(this.resolveApiUrl(url));
  }

  private buildPainelParams(request: PainelRequest): HttpParams {
    let params = new HttpParams();

    params = this.appendArrayParams(params, 'contratoIds', request.contratoIds);
    params = this.appendArrayParams(params, 'servicoIds', request.servicoIds);
    params = this.appendOptionalParam(params, 'grupo', request.grupo);
    params = this.appendOptionalParam(params, 'setor', request.setor);
    params = this.appendOptionalParam(params, 'contratoNome', request.contratoNome);
    params = this.appendOptionalParam(params, 'servicoNome', request.servicoNome);

    return params;
  }

  private appendOptionalParam(params: HttpParams, key: string, value: string | undefined): HttpParams {
    return value ? params.set(key, value) : params;
  }

  private appendArrayParams(params: HttpParams, key: string, values: number[]): HttpParams {
    return values.reduce((currentParams, value) => currentParams.append(key, value), params);
  }

  private resolveApiUrl(path: string): string {
    return path.startsWith('/') ? path : `/${path}`;
  }
}

export type {
  ApiResponse,
  AuthLoginPayload,
  AuthLoginResult,
  ContratoArquivoResponse,
  ContratoDetalhe,
  ContratoOption,
  ContratoRequestPayload,
  ContratoResponse,
  ContratoServicoRequestPayload,
  ContratoServicoResponse,
  DashboardMetrics,
  PainelFiltros,
  PainelLinha,
  PainelOperacional,
  PainelRequest,
  PainelResumo,
  RepasseItem,
  RepasseRequestPayload,
  ServicoDetalhe,
  ServicoOption,
  ServicoRequestPayload,
  ServicoResponse,
  SessaoUsuario,
  SoftwareResumo
} from './painel-operacional.models';
