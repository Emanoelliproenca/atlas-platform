import { TestBed } from '@angular/core/testing';
import { AuthSessionService } from './auth-session.service';

describe('AuthSessionService', () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    TestBed.resetTestingModule();
  });

  it('stores connection preferences and authenticated session in sessionStorage', () => {
    TestBed.configureTestingModule({
      providers: [AuthSessionService]
    });

    const service = TestBed.inject(AuthSessionService);

    service.save({
      baseUrl: 'http://localhost:8091/',
      username: 'admin',
      token: 'token-seguro',
      roles: ['ROLE_ADMIN'],
      expiraEm: '2026-04-23T15:00:00Z'
    });

    expect(service.isAuthenticated()).toBe(true);
    expect(localStorage.getItem('atlas.connection')).toBeNull();
    expect(sessionStorage.getItem('atlas.connection')).toBe(
      JSON.stringify({
        baseUrl: 'http://localhost:8091',
        username: 'admin'
      })
    );
    expect(localStorage.getItem('atlas.session')).toBeNull();
    expect(sessionStorage.getItem('atlas.session')).toBe(
      JSON.stringify({
        baseUrl: 'http://localhost:8091',
        username: 'admin',
        token: 'token-seguro',
        roles: ['ROLE_ADMIN'],
        expiraEm: '2026-04-23T15:00:00Z'
      })
    );
  });

  it('loads saved connection without restoring authentication', () => {
    sessionStorage.setItem(
      'atlas.connection',
      JSON.stringify({
        baseUrl: 'http://localhost:8091/',
        username: 'viewer'
      })
    );

    TestBed.configureTestingModule({
      providers: [AuthSessionService]
    });

    const service = TestBed.inject(AuthSessionService);

    expect(service.preferences()).toEqual({
      baseUrl: 'http://localhost:8091',
      username: 'viewer'
    });
    expect(service.isAuthenticated()).toBe(false);
    expect(service.session()).toBeNull();
  });

  it('resets frontend dev server URLs saved as API preference', () => {
    sessionStorage.setItem(
      'atlas.connection',
      JSON.stringify({
        baseUrl: 'http://127.0.0.1:4311',
        username: 'admin'
      })
    );

    TestBed.configureTestingModule({
      providers: [AuthSessionService]
    });

    const service = TestBed.inject(AuthSessionService);

    expect(service.preferences()).toEqual({
      baseUrl: 'http://localhost:8091',
      username: 'admin'
    });
  });

  it('restores authenticated session from sessionStorage', () => {
    sessionStorage.setItem(
      'atlas.session',
      JSON.stringify({
        baseUrl: 'http://localhost:8091/',
        username: 'admin',
        token: 'token-seguro',
        roles: ['ROLE_ADMIN'],
        expiraEm: '2026-04-23T15:00:00Z'
      })
    );

    TestBed.configureTestingModule({
      providers: [AuthSessionService]
    });

    const service = TestBed.inject(AuthSessionService);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.session()).toEqual({
      baseUrl: 'http://localhost:8091',
      username: 'admin',
      token: 'token-seguro',
      roles: ['ROLE_ADMIN'],
      expiraEm: '2026-04-23T15:00:00Z'
    });
  });

  it('does not restore authenticated session from localStorage', () => {
    localStorage.setItem(
      'atlas.session',
      JSON.stringify({
        baseUrl: 'http://localhost:8091/',
        username: 'admin',
        token: 'token-seguro',
        roles: ['ROLE_ADMIN'],
        expiraEm: '2026-04-23T15:00:00Z'
      })
    );

    TestBed.configureTestingModule({
      providers: [AuthSessionService]
    });

    const service = TestBed.inject(AuthSessionService);

    expect(service.isAuthenticated()).toBe(false);
    expect(service.session()).toBeNull();
  });

  it('returns token and clears persisted session on logout', () => {
    TestBed.configureTestingModule({
      providers: [AuthSessionService]
    });

    const service = TestBed.inject(AuthSessionService);

    service.save({
      baseUrl: 'http://localhost:8091',
      username: 'admin',
      token: 'token-seguro',
      roles: ['ROLE_ADMIN'],
      expiraEm: null
    });

    expect(service.getToken()).toBe('token-seguro');

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(sessionStorage.getItem('atlas.session')).toBeNull();
  });
});
