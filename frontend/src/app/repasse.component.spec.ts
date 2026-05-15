import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { PainelOperacionalApi } from './painel-operacional-api';
import { RepasseComponent } from './repasse.component';
import { SESSION_EXPIRED_MESSAGE, provideAuthSession, provideEmptyRouter } from './testing/test-helpers';

describe('RepasseComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it('renders repasses loaded from the API', async () => {
    await TestBed.configureTestingModule({
      imports: [RepasseComponent],
      providers: [
        ...provideEmptyRouter(),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarRepasses: () =>
              of({
                sucesso: true,
                mensagem: 'ok',
                dados: [
                  {
                    id: 1,
                    titulo: 'Checklist de release demo',
                    categoria: 'Equipamentos',
                    conteudo: 'Troca planejada',
                    prioridade: 'Alta',
                    ativo: true
                  }
                ]
              })
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(RepasseComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Repasse');
    expect(compiled.textContent).toContain('Checklist de release demo');
    expect(compiled.textContent).toContain('Equipamentos');
    expect(compiled.textContent).toContain('ATLAS');
  });

  it('shows API error message when loading fails', async () => {
    await TestBed.configureTestingModule({
      imports: [RepasseComponent],
      providers: [
        ...provideEmptyRouter(),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarRepasses: () =>
              throwError(() => ({
                error: {
                  mensagem: 'Falha ao consultar repasses'
                }
              }))
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(RepasseComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Falha ao consultar repasses');
  });

  it('shows standard session message and clears auth on 401', async () => {
    const clear = vi.fn();

    await TestBed.configureTestingModule({
      imports: [RepasseComponent],
      providers: [
        ...provideEmptyRouter(),
        provideAuthSession('ROLE_VISUALIZADOR', clear),
        {
          provide: PainelOperacionalApi,
          useValue: {
            carregarRepasses: () => throwError(() => ({ status: 401 }))
          }
        }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(RepasseComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(clear).toHaveBeenCalled();
    expect(compiled.textContent).toContain(SESSION_EXPIRED_MESSAGE);
  });
});
