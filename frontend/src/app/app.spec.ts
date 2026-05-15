import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { PainelOperacionalApi } from './painel-operacional-api';
import { routes } from './app.routes';
import { createPainelOperacionalApiMock } from './testing/painel-operacional-api.mock';

describe('App', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it('should create the app', async () => {
    await configureApp();

    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render home title', async () => {
    sessionStorage.setItem(
      'atlas.session',
      JSON.stringify({
        baseUrl: 'http://localhost:8091',
        username: 'demo.admin',
        token: 'token-admin',
        roles: ['ROLE_ADMIN'],
        expiraEm: null
      })
    );
    await configureApp();

    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.home-copy h1')?.textContent).toContain('Bem-vinda ao ATLAS');
  });
});

async function configureApp(): Promise<void> {
  const api = createPainelOperacionalApiMock();

  await TestBed.configureTestingModule({
    imports: [App],
    providers: [
      provideRouter(routes),
      { provide: PainelOperacionalApi, useValue: api }
    ]
  }).compileComponents();
}
