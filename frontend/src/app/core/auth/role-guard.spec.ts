import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { roleGuard } from './role-guard';

describe('roleGuard', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  afterEach(() => sessionStorage.clear());

  function signIn(role: string): void {
    sessionStorage.setItem('hub-event.session.v1', JSON.stringify({ token: 'jwt-de-test', role }));
  }

  const run = (...roles: Array<'MEMBER' | 'ORGANIZER' | 'ADMIN'>) =>
    TestBed.runInInjectionContext(() =>
      roleGuard(...roles)({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

  it('laisse passer un utilisateur avec le bon rôle', () => {
    signIn('ADMIN');
    expect(run('ADMIN')).toBe(true);
  });

  it('redirige vers /connexion sans session', () => {
    const result = run('ADMIN');
    expect(result).toBeInstanceOf(UrlTree);
    expect(String(result)).toBe('/connexion');
  });

  it("redirige vers l'accueil si le rôle ne correspond pas", () => {
    signIn('MEMBER');
    const result = run('ADMIN');
    expect(result).toBeInstanceOf(UrlTree);
    expect(String(result)).toBe('/');
  });
});
