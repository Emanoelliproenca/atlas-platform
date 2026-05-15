import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthSessionService } from './auth-session.service';

export const adminGuard: CanActivateFn = () => {
  const session = inject(AuthSessionService).session();
  return session?.roles.includes('ROLE_ADMIN') ? true : inject(Router).createUrlTree(['/']);
};
