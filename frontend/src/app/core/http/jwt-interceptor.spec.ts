import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Auth } from '../auth/auth';
import { jwtInterceptor } from './jwt-interceptor';

describe('jwtInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([jwtInterceptor])), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
    sessionStorage.clear();
  });

  async function login(): Promise<void> {
    const auth = TestBed.inject(Auth);
    const login = auth.login('membre@test.fr', 'Secret123!');
    controller.expectOne('/api/auth/login').flush({ token: 'jwt-de-test', role: 'MEMBER' });
    await login;
  }

  it("n'ajoute pas d'en-tête Authorization sans session", () => {
    http.get('/api/events').subscribe();

    const req = controller.expectOne('/api/events');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('ajoute le jeton en Bearer sur les appels à notre API', async () => {
    await login();

    http.get('/api/events').subscribe();

    const req = controller.expectOne('/api/events');
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-de-test');
    req.flush({});
  });

  it("n'ajoute pas le jeton sur un appel hors de notre API", async () => {
    await login();

    http.get('https://exemple.fr/data').subscribe();

    const req = controller.expectOne('https://exemple.fr/data');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });
});
