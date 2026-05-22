import { Injectable, signal } from '@angular/core';
import { environment } from '../environments/environment';

export interface AuthSession {
  baseUrl: string;
  username: string;
  token: string;
  roles: string[];
  expiraEm: string | null;
}

interface StoredConnectionPreferences {
  baseUrl: string;
  username: string;
}

const STORAGE_KEY = 'atlas.connection';
const SESSION_STORAGE_KEY = 'atlas.session';
export const DEFAULT_API_URL = environment.apiUrl;
const LEGACY_API_URLS = new Set([
  'http://localhost:8080',
  'http://127.0.0.1:8080',
  'http://localhost:4200',
  'http://127.0.0.1:4200',
  'http://localhost:4300',
  'http://127.0.0.1:4300',
  'http://localhost:4311',
  'http://127.0.0.1:4311',
  'http://localhost:4312',
  'http://127.0.0.1:4312'
]);
const DEFAULT_PREFERENCES: StoredConnectionPreferences = {
  baseUrl: DEFAULT_API_URL,
  username: ''
};

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  private readonly sessionState = signal<AuthSession | null>(this.loadSession());
  private readonly preferencesState = signal<StoredConnectionPreferences>(this.loadStoredPreferences());

  readonly session = this.sessionState.asReadonly();
  readonly preferences = this.preferencesState.asReadonly();

  isAuthenticated(): boolean {
    const session = this.sessionState();
    return Boolean(session?.username && session.token);
  }

  save(session: AuthSession): void {
    const normalizedSession = this.normalizeSession(session);
    this.sessionState.set(normalizedSession);
    this.savePreferences(normalizedSession);
    sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(normalizedSession));
  }

  rememberConnection(baseUrl: string, username: string): void {
    const storedPreferences: StoredConnectionPreferences = {
      baseUrl: this.normalizeBaseUrl(baseUrl),
      username: username.trim()
    };

    this.preferencesState.set(storedPreferences);
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(storedPreferences));
  }

  currentBaseUrl(): string {
    return this.sessionState()?.baseUrl ?? this.preferencesState().baseUrl;
  }

  getToken(): string | null {
    return this.sessionState()?.token ?? null;
  }

  logout(): void {
    this.clear();
  }

  clear(): void {
    this.sessionState.set(null);
    sessionStorage.removeItem(SESSION_STORAGE_KEY);
  }

  private loadStoredPreferences(): StoredConnectionPreferences {
    const parsed = this.parseStorageItem<StoredConnectionPreferences>(sessionStorage.getItem(STORAGE_KEY));

    return {
      baseUrl: this.normalizeBaseUrl(parsed?.baseUrl),
      username: (parsed?.username ?? DEFAULT_PREFERENCES.username).trim()
    };
  }

  private normalizeSession(session: AuthSession): AuthSession {
    return {
      ...session,
      baseUrl: this.normalizeBaseUrl(session.baseUrl),
      token: session.token.trim(),
      expiraEm: session.expiraEm ?? null,
      roles: Array.isArray(session.roles) ? session.roles : []
    };
  }

  private loadSession(): AuthSession | null {
    const parsed = this.parseStorageItem<AuthSession>(sessionStorage.getItem(SESSION_STORAGE_KEY));
    if (!this.isValidStoredSession(parsed)) {
      return null;
    }

    return this.normalizeSession({
      baseUrl: parsed.baseUrl,
      username: parsed.username,
      token: parsed.token,
      roles: Array.isArray(parsed.roles) ? parsed.roles : [],
      expiraEm: typeof parsed.expiraEm === 'string' ? parsed.expiraEm : null
    });
  }

  private savePreferences(session: AuthSession): void {
    this.rememberConnection(session.baseUrl, session.username);
  }

  private normalizeBaseUrl(baseUrl: string | null | undefined): string {
    const normalizedValue = (baseUrl ?? '').trim().replace(/\/+$/, '');
    if (!normalizedValue || LEGACY_API_URLS.has(normalizedValue)) {
      return DEFAULT_API_URL;
    }

    return normalizedValue;
  }

  private parseStorageItem<T>(value: string | null): Partial<T> | null {
    if (!value) {
      return null;
    }

    try {
      return JSON.parse(value) as Partial<T>;
    } catch {
      return null;
    }
  }

  private isValidStoredSession(session: Partial<AuthSession> | null): session is AuthSession {
    return Boolean(session?.baseUrl && session.username && session.token);
  }
}
