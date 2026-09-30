import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let message: string;

      switch (error.status) {
        case 400:
          message = typeof error.error === 'string' ? error.error : 'La requête est invalide.';
          break;

        case 401:
          message = 'Votre session a expiré. Veuillez vous reconnecter.';
          void router.navigate(['/connexion']);
          break;

        case 403:
          message = 'Accès refusé.';
          break;

        case 404:
          message = 'La ressource demandée est introuvable.';
          break;

        case 500:
          message = 'Une erreur interne est survenue.';
          break;

        default:
          message = 'Une erreur est survenue.';
      }

      return throwError(
        () =>
          new HttpErrorResponse({
            error: message,
            headers: error.headers,
            status: error.status,
            statusText: error.statusText,
            url: error.url ?? undefined,
          }),
      );
    }),
  );
};
