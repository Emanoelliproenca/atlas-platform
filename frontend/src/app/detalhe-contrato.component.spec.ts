import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { AuthSessionService } from './auth-session.service';
import { DetalheContratoComponent } from './detalhe-contrato.component';
import { PainelOperacionalApi } from './painel-operacional-api';
import { provideAuthSession, provideEmptyRouter, provideRouteId } from './testing/test-helpers';

describe('DetalheContratoComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it('renders contract detail and linked services', async () => {
    await TestBed.configureTestingModule({
      imports: [DetalheContratoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(7),
        provideAuthSession('ROLE_VISUALIZADOR'),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarContratoDetalhe: () =>
              of({
                sucesso: true,
                mensagem: 'ok',
                dados: {
                  contrato: {
                    id: 7,
                    nome: 'Contrato Atlas',
                    grupo: 'Operacoes',
                    centralUrl: 'https://atlas-demo.example.invalid',
                    particularidades: 'Operacao 24x7',
                    documentacao: 'Runbook Atlas',
                    ativo: true
                  },
                  servicos: [
                    {
                      id: 10,
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
              }),
            atualizarContrato: () => of()
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheContratoComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Contrato • Operacoes • Ativo');
    expect(compiled.textContent).toContain('Servicos vinculados (1)');
    expect(compiled.textContent).toContain('Contrato Atlas');
    expect(compiled.textContent).toContain('Operacoes');
    expect(compiled.textContent).toContain('Analytics Operacional');
    expect(compiled.textContent).toContain('Runbook Atlas');
  });

  it('saves documentation changes for admin users', async () => {
    const atualizarContrato = vi.fn().mockReturnValue(
      of({
        sucesso: true,
        mensagem: 'ok',
        dados: {
          id: 7,
          nome: 'Contrato Atlas',
          grupo: 'Operacoes',
          centralUrl: 'https://atlas-demo.example.invalid',
          particularidades: 'Atualizado',
          documentacao: 'Novo runbook',
          ativo: true
        }
      })
    );

    await TestBed.configureTestingModule({
      imports: [DetalheContratoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(7),
        provideAuthSession('ROLE_ADMIN'),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarContratoDetalhe: () =>
              of({
                sucesso: true,
                mensagem: 'ok',
                dados: {
                  contrato: {
                    id: 7,
                    nome: 'Contrato Atlas',
                    grupo: 'Operacoes',
                    centralUrl: 'https://atlas-demo.example.invalid',
                    particularidades: 'Original',
                    documentacao: 'Original doc',
                    ativo: true
                  },
                  servicos: []
                }
              }),
            atualizarContrato
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheContratoComponent);
    const component = fixture.componentInstance as unknown as {
      particularidades: { set: (value: string) => void };
      documentacao: { set: (value: string) => void };
      salvarDocumentacao: () => void;
      mensagemSucesso: () => string;
    };

    await fixture.whenStable();
    component.particularidades.set('Atualizado');
    component.documentacao.set('Novo runbook');
    component.salvarDocumentacao();
    await fixture.whenStable();

    expect(atualizarContrato).toHaveBeenCalledWith(7, {
      nome: 'Contrato Atlas',
      grupo: 'Operacoes',
      centralUrl: 'https://atlas-demo.example.invalid',
      particularidades: 'Atualizado',
      documentacao: 'Novo runbook',
      ativo: true
    });
    expect(component.mensagemSucesso()).toBe('Documentacao atualizada com sucesso.');
  });

  it('keeps save action disabled while there are no documentation changes', async () => {
    await TestBed.configureTestingModule({
      imports: [DetalheContratoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(7),
        provideAuthSession('ROLE_ADMIN'),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarContratoDetalhe: () =>
              of({
                sucesso: true,
                mensagem: 'ok',
                dados: {
                  contrato: {
                    id: 7,
                    nome: 'Contrato Atlas',
                    grupo: 'Operacoes',
                    centralUrl: 'https://atlas-demo.example.invalid',
                    particularidades: 'Original',
                    documentacao: 'Original doc',
                    ativo: true
                  },
                  servicos: []
                }
              }),
            atualizarContrato: vi.fn()
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheContratoComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;
    const saveButton = Array.from(host.querySelectorAll('button'))
      .find((element) => element.textContent?.includes('Salvar documentacao')) as HTMLButtonElement;

    expect(saveButton.disabled).toBe(true);
  });

  it('enables save action and updates helper text after editing documentation', async () => {
    await TestBed.configureTestingModule({
      imports: [DetalheContratoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(7),
        provideAuthSession('ROLE_ADMIN'),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarContratoDetalhe: () =>
              of({
                sucesso: true,
                mensagem: 'ok',
                dados: {
                  contrato: {
                    id: 7,
                    nome: 'Contrato Atlas',
                    grupo: 'Operacoes',
                    centralUrl: 'https://atlas-demo.example.invalid',
                    particularidades: 'Original',
                    documentacao: 'Original doc',
                    ativo: true
                  },
                  servicos: []
                }
              }),
            atualizarContrato: vi.fn()
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheContratoComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const host = fixture.nativeElement as HTMLElement;
    const textareas = host.querySelectorAll('textarea');
    const documentacaoTextarea = textareas[1] as HTMLTextAreaElement;
    documentacaoTextarea.value = 'Original doc atualizado';
    documentacaoTextarea.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    fixture.detectChanges();

    const saveButton = Array.from(host.querySelectorAll('button'))
      .find((element) => element.textContent?.includes('Salvar documentacao')) as HTMLButtonElement;

    expect(saveButton.disabled).toBe(false);
  });

  it('shows invalid contract message when route id is missing', async () => {
    await TestBed.configureTestingModule({
      imports: [DetalheContratoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(),
        provideAuthSession('ROLE_VISUALIZADOR'),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarContratoDetalhe: vi.fn(),
            atualizarContrato: vi.fn()
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheContratoComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Contrato invalido.');
  });

  it('shows API fallback message when contract detail fails without custom message', async () => {
    await TestBed.configureTestingModule({
      imports: [DetalheContratoComponent],
      providers: [
        ...provideEmptyRouter(),
        provideRouteId(7),
        provideAuthSession('ROLE_VISUALIZADOR'),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarContratoDetalhe: () => throwError(() => ({})),
            atualizarContrato: vi.fn()
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(DetalheContratoComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Nao foi possivel carregar o detalhe do contrato.');
  });
});
