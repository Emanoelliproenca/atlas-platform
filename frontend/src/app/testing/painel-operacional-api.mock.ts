import { of } from 'rxjs';
import { vi } from 'vitest';

export function createPainelOperacionalApiMock() {
  return {
    login: vi.fn().mockReturnValue(
      of({
        sucesso: true,
        mensagem: 'Sessao iniciada com sucesso',
        dados: {
          token: 'token-seguro',
          username: 'demo.admin',
          roles: [],
          expiraEm: '2026-04-23T15:00:00Z'
        }
      })
    ),
    carregarSessao: vi.fn().mockReturnValue(
      of({
        sucesso: true,
        mensagem: 'Sessao carregada com sucesso',
        dados: {
          username: 'demo.admin',
          roles: [],
          expiraEm: '2026-04-23T15:00:00Z'
        }
      })
    ),
    logout: vi.fn().mockReturnValue(of({ sucesso: true, mensagem: 'ok', dados: null })),
    carregarPainel: vi.fn().mockReturnValue(
      of({
        sucesso: true,
        mensagem: 'Consulta operacional carregada com sucesso',
        dados: {
          resumo: {
            totalRelacionamentos: 1,
            totalContratos: 1,
            totalServicos: 1,
            totalGrupos: 1,
            totalSetores: 1
          },
          filtros: {
            contratos: [],
            servicos: [],
            grupos: ['Operacoes Demo'],
            setores: ['Suporte']
          },
          linhas: []
        }
      })
    ),
    listarContratos: vi.fn().mockReturnValue(
      of({
        sucesso: true,
        mensagem: 'ok',
        dados: [
          {
            id: 1,
            nome: 'Contrato Aurora Demo',
            grupo: 'Operacoes',
            centralUrl: 'https://aurora-demo.example.invalid',
            particularidades: 'Cenario ficticio para validacao visual.',
            documentacao: 'Playbook demo sem dados reais.',
            ativo: true
          }
        ]
      })
    ),
    listarServicos: vi.fn().mockReturnValue(
      of({
        sucesso: true,
        mensagem: 'ok',
        dados: [
          {
            id: 2,
            nome: 'Analytics Operacional',
            descricaoSoftware: 'Suite ATLAS Core',
            ativo: true
          }
        ]
      })
    ),
    listarRelacionamentos: vi.fn().mockReturnValue(
      of({
        sucesso: true,
        mensagem: 'ok',
        dados: [
          {
            id: 3,
            contratoId: 1,
            contratoNome: 'Contrato Aurora Demo',
            servicoId: 2,
            servicoNome: 'Analytics Operacional',
            versao: '2026.3',
            observacao: 'Cenario principal do ambiente demonstrativo.',
            setor: 'Operacoes Corporativas',
            ativo: true
          }
        ]
      })
    ),
    criarContrato: vi.fn().mockReturnValue(
      of({
        sucesso: true,
        mensagem: 'ok',
        dados: {
          id: 4,
          nome: 'Novo contrato demo',
          grupo: 'Operacoes',
          centralUrl: 'https://novo-demo.example.invalid',
          particularidades: '',
          documentacao: '',
          ativo: true
        }
      })
    ),
    atualizarContrato: vi.fn(),
    ativarContrato: vi.fn(),
    inativarContrato: vi.fn().mockReturnValue(of({ sucesso: true, mensagem: 'ok', dados: null })),
    criarServico: vi.fn(),
    atualizarServico: vi.fn(),
    ativarServico: vi.fn(),
    inativarServico: vi.fn(),
    criarRelacionamento: vi.fn(),
    atualizarRelacionamento: vi.fn(),
    ativarRelacionamento: vi.fn(),
    inativarRelacionamento: vi.fn(),
    deletarRelacionamento: vi.fn()
  };
}
