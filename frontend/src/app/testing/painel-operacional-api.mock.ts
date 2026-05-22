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
          username: 'admin',
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
          username: 'admin',
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
    carregarDashboardMetrics: vi.fn().mockReturnValue(
      of({
        sucesso: true,
        mensagem: 'Métricas do dashboard carregadas com sucesso',
        dados: {
          totalContratos: 1,
          contratosAtivos: 1,
          contratosInativos: 0,
          totalServicos: 1,
          servicosAtivos: 1,
          servicosInativos: 0,
          totalRelacionamentos: 1,
          relacionamentosAtivos: 1,
          repassesAtivos: 1,
          softwaresAtivos: 1,
          softwaresDesatualizados: 0,
          percentualContratosComDocumentacao: 100,
          percentualContratosComServicos: 100,
          contratosPorGrupo: [{ nome: 'Operacoes Demo', total: 1 }],
          servicosPorSetor: [{ nome: 'Suporte', total: 1 }],
          ultimosContratos: [{ id: 1, nome: 'Contrato Atlas Labs Demo', grupo: 'Operacoes', ativo: true }],
          ultimosRepasses: [
            {
              id: 1,
              titulo: 'Repasse demo',
              categoria: 'Informativo',
              prioridade: 'Baixa',
              criadoEm: '2026-05-21T12:00:00Z'
            }
          ]
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
            nome: 'Contrato Atlas Labs Demo',
            grupo: 'Operacoes',
            centralUrl: 'https://atlas-labs-demo.example.invalid',
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
            versaoReferencia: '2026.3',
            linkSoftware: 'https://analytics-atlas-labs.example.invalid',
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
            contratoNome: 'Contrato Atlas Labs Demo',
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
    criarSoftware: vi.fn(),
    atualizarSoftware: vi.fn(),
    ativarSoftware: vi.fn(),
    inativarSoftware: vi.fn(),
    carregarSoftwares: vi.fn().mockReturnValue(of({ sucesso: true, mensagem: 'ok', dados: [] })),
    baixarSoftware: vi.fn(),
    criarRelacionamento: vi.fn(),
    atualizarRelacionamento: vi.fn(),
    ativarRelacionamento: vi.fn(),
    inativarRelacionamento: vi.fn(),
    deletarRelacionamento: vi.fn()
  };
}
