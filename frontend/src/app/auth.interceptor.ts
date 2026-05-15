import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthSessionService } from './auth-session.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authSessionService = inject(AuthSessionService);
  const apiUrl = authSessionService.currentBaseUrl();

  const finalUrl = request.url.startsWith('http')
    ? request.url
    : `${apiUrl}${normalizeRequestPath(request.url)}`;

  if (isLoginRequest(finalUrl)) {
    return next(request.clone({ url: finalUrl }));
  }

  const token = authSessionService.getToken();
  if (!token) {
    return next(request.clone({ url: finalUrl }));
  }

  return next(
    request.clone({
      url: finalUrl,
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
  );
};

function normalizeRequestPath(url: string): string {
  return url.startsWith('/') ? url : `/${url}`;
}

function isLoginRequest(url: string): boolean {
  return /\/auth\/login$/.test(url);
}
