import { Injectable, inject } from '@angular/core';
import { AuthSessionService } from './auth-session.service';

interface ApiErrorShape {
  status?: number;
  error?: {
    mensagem?: string;
  };
}

export const SESSION_EXPIRED_MESSAGE =
  'Sessao expirada ou credenciais invalidas. Entre novamente para continuar.';

@Injectable({ providedIn: 'root' })
export class ApiFeedbackService {
  private readonly authSession = inject(AuthSessionService);

  mensagem(error: ApiErrorShape | null | undefined, fallback: string): string {
    if (error?.status === 401) {
      this.authSession.clear();
      return SESSION_EXPIRED_MESSAGE;
    }

    return error?.error?.mensagem ?? fallback;
  }
}
