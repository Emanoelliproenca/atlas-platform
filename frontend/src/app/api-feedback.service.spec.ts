import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { ApiFeedbackService } from './api-feedback.service';
import { AuthSessionService } from './auth-session.service';

describe('ApiFeedbackService', () => {
  it('clears session and returns standard message on 401', () => {
    const clear = vi.fn();

    TestBed.configureTestingModule({
      providers: [
        ApiFeedbackService,
        {
          provide: AuthSessionService,
          useValue: {
            clear
          }
        }
      ]
    });

    const service = TestBed.inject(ApiFeedbackService);
    const mensagem = service.mensagem({ status: 401 }, 'fallback');

    expect(clear).toHaveBeenCalled();
    expect(mensagem).toBe('Sessao expirada ou credenciais invalidas. Entre novamente para continuar.');
  });

  it('returns API message when available', () => {
    TestBed.configureTestingModule({
      providers: [
        ApiFeedbackService,
        {
          provide: AuthSessionService,
          useValue: {
            clear: vi.fn()
          }
        }
      ]
    });

    const service = TestBed.inject(ApiFeedbackService);
    const mensagem = service.mensagem({ error: { mensagem: 'Falha especifica' } }, 'fallback');

    expect(mensagem).toBe('Falha especifica');
  });
});
