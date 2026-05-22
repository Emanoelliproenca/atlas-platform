import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { DetalheServicoComponent } from './detalhe-servico.component';
import { PainelOperacionalApi } from './painel-operacional-api';
import { provideEmptyRouter, provideRouteId } from './testing/test-helpers';

describe('DetalheServicoComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it('renders service detail and linked contracts', async () => {
    await TestBed.configureTestingModule({
      imports: [DetalheServicoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(3),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarServicoDetalhe: () =>
              of({
                sucesso: true,
                mensagem: 'ok',
                dados: {
                  servico: {
                    id: 3,
                    nome: 'Analytics Operacional',
                    descricaoSoftware: 'Suite ATLAS Core',
                    ativo: true
                  },
                  contratos: [
                    {
                      id: 11,
                      contratoId: 7,
                      contratoNome: 'Contrato Atlas',
                      servicoId: 3,
                      servicoNome: 'Analytics Operacional',
                      versao: '2026.3',
                      observacao: 'Principal',
                      setor: 'Operacoes Corporativas',
                      ativo: true
                    }
                  ]
                }
              })
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheServicoComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Detalhe operacional do serviço');
    expect(compiled.textContent).toContain('Analytics Operacional');
    expect(compiled.textContent).toContain('Suite ATLAS Core');
    expect(compiled.textContent).toContain('Contrato Atlas');
    expect(compiled.textContent).toContain('2026.3');
    expect(compiled.textContent).toContain('Este serviço aparece em 1 contrato da base operacional.');
  });

  it('renders empty relationship summary when service has no linked contracts', async () => {
    await TestBed.configureTestingModule({
      imports: [DetalheServicoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(3),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarServicoDetalhe: () =>
              of({
                sucesso: true,
                mensagem: 'ok',
                dados: {
                  servico: {
                    id: 3,
                    nome: 'Analytics Operacional',
                    descricaoSoftware: 'Suite ATLAS Core',
                    ativo: true
                  },
                  contratos: []
                }
              })
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheServicoComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Nenhum contrato vinculado por enquanto.');
    expect(compiled.textContent).toContain('Este serviço ainda não está vinculado a contratos. Assim que houver relacionamento, a matriz operacional aparece aqui.');
  });

  it('shows API error message when service detail fails to load', async () => {
    await TestBed.configureTestingModule({
      imports: [DetalheServicoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(3),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarServicoDetalhe: () =>
              throwError(() => ({
                error: {
                mensagem: 'Serviço não encontrado'
                }
              }))
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheServicoComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Serviço não encontrado');
  });

  it('shows invalid service message when route id is missing', async () => {
    const carregarServicoDetalhe = vi.fn();

    await TestBed.configureTestingModule({
      imports: [DetalheServicoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarServicoDetalhe
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheServicoComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    expect(carregarServicoDetalhe).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Serviço inválido.');
  });

  it('shows fallback message when service detail fails without API message', async () => {
    await TestBed.configureTestingModule({
      imports: [DetalheServicoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(3),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarServicoDetalhe: () => throwError(() => ({}))
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheServicoComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Não foi possível carregar o detalhe do serviço.');
  });
});
