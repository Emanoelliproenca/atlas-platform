import { EnvironmentProviders, Provider } from '@angular/core';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { SESSION_EXPIRED_MESSAGE } from '../api-feedback.service';
import { AuthSessionService } from '../auth-session.service';

type Role = 'ROLE_ADMIN' | 'ROLE_VISUALIZADOR';

export { SESSION_EXPIRED_MESSAGE };

export function provideEmptyRouter(): Array<Provider | EnvironmentProviders> {
  return [provideRouter([])];
}

export function provideRouteId(id?: number | string): Provider {
  return {
    provide: ActivatedRoute,
    useValue: {
      snapshot: {
        paramMap: convertToParamMap(id == null ? {} : { id: String(id) })
      }
    }
  };
}

export function createSession(role: Role, username = role === 'ROLE_ADMIN' ? 'admin' : 'viewer') {
  return {
    baseUrl: 'http://localhost:8091',
    username,
    token: role === 'ROLE_ADMIN' ? 'token-admin' : 'token-view',
    roles: [role]
  };
}

export function provideAuthSession(role: Role, clear = vi.fn()): Provider {
  return {
    provide: AuthSessionService,
    useValue: {
      clear,
      session: () => createSession(role)
    }
  };
}
