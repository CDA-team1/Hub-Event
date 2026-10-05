import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';

import { adminGuard } from './admin-guard';

describe('adminGuard', () => {
  beforeEach(() => {
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  afterEach(() => sessionStorage.clear());

  const run = () =>
    TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

  it('laisse passer un administrateur', () => {
    sessionStorage.setItem(
      'hub-event.session.v1',
      JSON.stringify({ token: 'jwt-de-test', role: 'ADMIN' }),
    );

    expect(run()).toBe(true);
  });

  it('redirige vers / pour un utilisateur connecté non administrateur', () => {
    sessionStorage.setItem(
      'hub-event.session.v1',
      JSON.stringify({ token: 'jwt-de-test', role: 'MEMBER' }),
    );

    const result = run();

    expect(result).toBeInstanceOf(UrlTree);
    expect(String(result)).toBe('/');
  });

  it('redirige vers /connexion sans session', () => {
    const result = run();

    expect(result).toBeInstanceOf(UrlTree);
    expect(String(result)).toBe('/connexion');
  });
});
