import { TestBed } from '@angular/core/testing';
import { CadastrosComponent } from './cadastros.component';
import { PainelOperacionalApi } from './painel-operacional-api';
import { provideAuthSession, provideEmptyRouter } from './testing/test-helpers';
import { createPainelOperacionalApiMock } from './testing/painel-operacional-api.mock';

describe('CadastrosComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it('renderiza a area administrativa para admin', async () => {
    const api = createPainelOperacionalApiMock();

    await TestBed.configureTestingModule({
      imports: [CadastrosComponent],
      providers: [
        ...provideEmptyRouter(),
        { provide: PainelOperacionalApi, useValue: api },
        provideAuthSession('ROLE_ADMIN')
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(CadastrosComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Gestão da base operacional');
    expect(compiled.textContent).toContain('Contratos cadastrados');
    expect(compiled.textContent).toContain('Contrato Atlas Labs Demo');
    expect(api.listarContratos).toHaveBeenCalled();
  });

  it('abre drawer de novo contrato e salva', async () => {
    const api = createPainelOperacionalApiMock();

    await TestBed.configureTestingModule({
      imports: [CadastrosComponent],
      providers: [
        ...provideEmptyRouter(),
        { provide: PainelOperacionalApi, useValue: api },
        provideAuthSession('ROLE_ADMIN')
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(CadastrosComponent);
    const component = fixture.componentInstance as unknown as {
      abrirDrawerContrato: (modo: 'novo') => void;
      contratoForm: {
        nome: string;
        grupo: string;
        centralUrl: string;
        particularidades: string;
        documentacao: string;
        ativo: boolean;
      };
      salvarDrawer: () => void;
      mensagem: () => string;
    };

    component.abrirDrawerContrato('novo');
    component.contratoForm.nome = 'Novo contrato';
    component.contratoForm.grupo = 'Operacoes';
    component.contratoForm.centralUrl = 'https://novo-demo.example.invalid';
    component.contratoForm.ativo = true;

    component.salvarDrawer();
    await fixture.whenStable();

    expect(api.criarContrato).toHaveBeenCalledWith({
      nome: 'Novo contrato',
      grupo: 'Operacoes',
      centralUrl: 'https://novo-demo.example.invalid',
      particularidades: '',
      documentacao: '',
      ativo: true
    });
    expect(component.mensagem()).toBe('Registro salvo com sucesso.');
  });

  it('inativa contrato ativo', async () => {
    const api = createPainelOperacionalApiMock();

    await TestBed.configureTestingModule({
      imports: [CadastrosComponent],
      providers: [
        ...provideEmptyRouter(),
        { provide: PainelOperacionalApi, useValue: api },
        provideAuthSession('ROLE_ADMIN')
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(CadastrosComponent);
    await fixture.whenStable();

    const component = fixture.componentInstance as unknown as {
      alternarContrato: (item: { id: number; ativo: boolean }) => void;
      mensagem: () => string;
    };

    component.alternarContrato({ id: 1, ativo: true });
    await fixture.whenStable();

    expect(api.inativarContrato).toHaveBeenCalledWith(1);
    expect(component.mensagem()).toBe('Registro inativado com sucesso.');
  });

  it('mostra estado restrito para nao admin', async () => {
    const api = createPainelOperacionalApiMock();

    await TestBed.configureTestingModule({
      imports: [CadastrosComponent],
      providers: [
        ...provideEmptyRouter(),
        { provide: PainelOperacionalApi, useValue: api },
        provideAuthSession('ROLE_VISUALIZADOR', undefined)
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(CadastrosComponent);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Você não tem permissão para acessar esta área.');
    expect(api.listarContratos).not.toHaveBeenCalled();
  });
});
