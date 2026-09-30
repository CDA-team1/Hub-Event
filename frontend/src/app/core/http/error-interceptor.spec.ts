import { HttpClient, HttpContext, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Auth } from '../auth/auth';
import { errorInterceptor } from './error-interceptor';
import { SKIP_AUTH_REDIRECT } from './skip-auth-redirect';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    controller.verify();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  function expectErrorMessage(): Promise<string> {
    return new Promise((resolve) => {
      http.get('/api/quelque-chose').subscribe({ error: (error) => resolve(error.error) });
    });
  }

  it('garde le message du back sur une 400', async () => {
    const message = expectErrorMessage();
    controller
      .expectOne('/api/quelque-chose')
      .flush('Message métier du back.', { status: 400, statusText: 'Bad Request' });

    expect(await message).toBe('Message métier du back.');
  });

  it("propose un message générique pour une 400 sans corps texte", async () => {
    const message = expectErrorMessage();
    controller
      .expectOne('/api/quelque-chose')
      .flush({ some: 'json' }, { status: 400, statusText: 'Bad Request' });

    expect(await message).toBe('La requête est invalide.');
  });

  it('déconnecte et redirige vers /connexion sur une 401 normale', async () => {
    const auth = TestBed.inject(Auth);
    const login = auth.login('membre@test.fr', 'Secret123!');
    controller.expectOne('/api/auth/login').flush({ token: 'jwt-de-test', role: 'MEMBER' });
    await login;
    expect(auth.isAuthenticated()).toBe(true);

    const navigate = vi.spyOn(router, 'navigate');
    const message = expectErrorMessage();
    controller
      .expectOne('/api/quelque-chose')
      .flush('peu importe', { status: 401, statusText: 'Unauthorized' });

    expect(await message).toBe('Votre session a expiré. Veuillez vous reconnecter.');
    expect(auth.isAuthenticated()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/connexion']);
  });

  it('garde le message du back et ne redirige pas quand SKIP_AUTH_REDIRECT est posé', async () => {
    const navigate = vi.spyOn(router, 'navigate');
    const message = new Promise<string>((resolve) => {
      http
        .get('/api/auth/login', { context: new HttpContext().set(SKIP_AUTH_REDIRECT, true) })
        .subscribe({ error: (error) => resolve(error.error) });
    });
    controller
      .expectOne('/api/auth/login')
      .flush('Email ou mot de passe invalide.', { status: 401, statusText: 'Unauthorized' });

    expect(await message).toBe('Email ou mot de passe invalide.');
    expect(navigate).not.toHaveBeenCalled();
  });

  it.each([
    [403, 'Accès refusé.'],
    [404, 'La ressource demandée est introuvable.'],
    [500, 'Une erreur interne est survenue.'],
    [418, 'Une erreur est survenue.'],
  ])('associe le bon message pour une %i', async (status, expected) => {
    const message = expectErrorMessage();
    controller.expectOne('/api/quelque-chose').flush('peu importe', { status, statusText: 'Error' });

    expect(await message).toBe(expected);
  });
});
