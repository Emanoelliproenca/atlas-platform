import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { AuthSessionService } from './auth-session.service';
import { PainelOperacionalApi } from './painel-operacional-api';
import { SoftwaresComponent } from './softwares.component';
import { SESSION_EXPIRED_MESSAGE, provideAuthSession, provideEmptyRouter } from './testing/test-helpers';

describe('SoftwaresComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it('renderiza a central de versoes e lista softwares', async () => {
    await TestBed.configureTestingModule({
      imports: [SoftwaresComponent],
      providers: [
        ...provideEmptyRouter(),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarSoftwares: () =>
              of({
                sucesso: true,
                mensagem: 'ok',
                dados: [
                  {
                    id: 1,
                    nome: 'atlas-demo-agent.jar',
                    descricaoSoftware: 'Artefato ficticio usado para simular coleta de eventos operacionais no ambiente demo.',
                    versaoReferencia: '1.0.8',
                    totalContratos: 4,
                    ativo: true
                  }
                ]
              })
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(SoftwaresComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const component = fixture.componentInstance as unknown as {
      selecionarSoftware: (id: number) => void;
    };

    component.selecionarSoftware(1);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Sistemas e serviços digitais');
    expect(compiled.textContent).toContain('Consulta de versão');
    expect(compiled.textContent).toContain('atlas-demo-agent.jar');
    expect(compiled.textContent).toContain('1.0.8');
    expect(compiled.textContent).toContain('Disponível');
    expect(compiled.textContent).toContain('Base operacional ATLAS');
  });

  it('mostra estado vazio quando nenhum software vem da API', async () => {
    await TestBed.configureTestingModule({
      imports: [SoftwaresComponent],
      providers: [
        ...provideEmptyRouter(),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarSoftwares: () =>
              of({
                sucesso: true,
                mensagem: 'ok',
                dados: []
              })
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(SoftwaresComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const content = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(content).toContain('Sistemas mapeados0');
    expect(content).toContain('Atualizados0');
    expect(content).toContain('Pendentes de revisão0');
    expect(content).toContain('Dados carregados em-');
    expect(content).toContain('Nenhum sistema encontrado');
  });

  it('mostra erro fallback quando a carga falha', async () => {
    await TestBed.configureTestingModule({
      imports: [SoftwaresComponent],
      providers: [
        ...provideEmptyRouter(),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarSoftwares: () => throwError(() => ({}))
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(SoftwaresComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Não foi possível carregar os sistemas.');
  });

  it('limpa sessao e mostra mensagem padrao em 401', async () => {
    const clear = vi.fn();

    await TestBed.configureTestingModule({
      imports: [SoftwaresComponent],
      providers: [
        ...provideEmptyRouter(),
        provideAuthSession('ROLE_ADMIN', clear),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarSoftwares: () => throwError(() => ({ status: 401 }))
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(SoftwaresComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    expect(clear).toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(SESSION_EXPIRED_MESSAGE);
  });
});
