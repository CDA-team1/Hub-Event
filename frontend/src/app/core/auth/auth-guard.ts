import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from './auth';

/** Redirige vers la connexion si l'utilisateur n'est pas authentifié. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  return auth.isAuthenticated() ? true : inject(Router).parseUrl('/connexion');
};
