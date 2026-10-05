import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../../domain/role';
import { Auth } from './auth';

/**
 * Autorise la navigation si l'utilisateur est connecté et a l'un des rôles donnés. Redirige vers
 * la connexion s'il n'est pas authentifié, vers l'accueil s'il n'a pas le bon rôle.
 */
export function roleGuard(...allowedRoles: Role[]): CanActivateFn {
  return () => {
    const auth = inject(Auth);
    const router = inject(Router);

    if (!auth.isAuthenticated()) {
      return router.parseUrl('/connexion');
    }
    if (allowedRoles.includes(auth.role()!)) {
      return true;
    }
    return router.parseUrl('/');
  };
}
