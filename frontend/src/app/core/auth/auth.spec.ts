import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SKIP_AUTH_REDIRECT } from '../http/skip-auth-redirect';
import { Auth } from './auth';

describe('Auth', () => {
  let auth: Auth;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    auth = TestBed.inject(Auth);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  it("n'est pas authentifié au départ", () => {
    expect(auth.isAuthenticated()).toBe(false);
    expect(auth.token()).toBeUndefined();
    expect(auth.role()).toBeUndefined();
  });

  it('mémorise la session en cas de connexion réussie', async () => {
    const login = auth.login('membre@test.fr', 'Secret123!');

    const req = http.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'membre@test.fr', password: 'Secret123!' });
    req.flush({ token: 'jwt-de-test', role: 'MEMBER' });
    await login;

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.token()).toBe('jwt-de-test');
    expect(auth.role()).toBe('MEMBER');
  });

  it('sauvegarde la session dans le sessionStorage, jamais dans le localStorage', async () => {
    const login = auth.login('membre@test.fr', 'Secret123!');
    http.expectOne('/api/auth/login').flush({ token: 'jwt-de-test', role: 'MEMBER' });
    await login;
    TestBed.tick();

    expect(sessionStorage.getItem('hub-event.session.v1')).toContain('jwt-de-test');
    expect(localStorage.getItem('hub-event.session.v1')).toBeNull();
  });

  it('marque la requête de login pour ignorer la redirection générique en cas de 401', () => {
    auth.login('membre@test.fr', 'mauvais-mot-de-passe').catch(() => undefined);

    const req = http.expectOne('/api/auth/login');
    expect(req.request.context.get(SKIP_AUTH_REDIRECT)).toBe(true);
    req.flush('Email ou mot de passe invalide.', { status: 401, statusText: 'Unauthorized' });
  });

  it("ne connecte pas l'utilisateur si les identifiants sont refusés", async () => {
    const login = auth.login('membre@test.fr', 'mauvais-mot-de-passe');
    http.expectOne('/api/auth/login').flush('Email ou mot de passe invalide.', {
      status: 401,
      statusText: 'Unauthorized',
    });

    await expect(login).rejects.toBeTruthy();
    expect(auth.isAuthenticated()).toBe(false);
  });

  it('efface la session à la déconnexion', async () => {
    const login = auth.login('membre@test.fr', 'Secret123!');
    http.expectOne('/api/auth/login').flush({ token: 'jwt-de-test', role: 'MEMBER' });
    await login;

    auth.logout();
    TestBed.tick();

    expect(auth.isAuthenticated()).toBe(false);
    expect(sessionStorage.getItem('hub-event.session.v1')).toBeNull();
  });
});
