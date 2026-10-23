import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth } from '../auth/auth';
import { API_URL } from './api-url';

/** Attache le jeton JWT courant en `Authorization: Bearer`, uniquement sur les appels à notre API. */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const apiUrl = inject(API_URL);
  const token = inject(Auth).token();

  if (!token || !req.url.startsWith(apiUrl)) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
