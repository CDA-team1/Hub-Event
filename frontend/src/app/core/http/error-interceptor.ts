import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/**
 * À poser sur une requête dont le 401 ne signifie pas "session expirée" (ex. l'appel de login
 * lui-même, où 401 veut dire "identifiants invalides") : désactive la redirection et le message
 * générique pour cette requête, le message réel du back est conservé tel quel.
 */
export const SKIP_AUTH_REDIRECT = new HttpContextToken<boolean>(() => false);

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
          if (req.context.get(SKIP_AUTH_REDIRECT)) {
            message = typeof error.error === 'string' ? error.error : 'Non autorisé.';
          } else {
            message = 'Votre session a expiré. Veuillez vous reconnecter.';
            void router.navigate(['/connexion']);
          }
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
