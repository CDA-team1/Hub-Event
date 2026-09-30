import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { LoginPage } from './login-page';

describe('LoginPage', () => {
  let element: HTMLElement;
  let http: HttpTestingController;

  // Un `await` supplémentaire (macrotâche) laisse le temps à la chaîne login() → catch →
  // serverError.set() de se terminer avant de vérifier le rendu : whenStable() seul ne suit
  // pas cette chaîne de promesses, propre au composant, en mode zoneless.
  const stable = async () => {
    await new Promise((resolve) => setTimeout(resolve));
    await TestBed.inject(ApplicationRef).whenStable();
  };

  beforeEach(async () => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    const fixture = TestBed.createComponent(LoginPage);
    element = fixture.nativeElement as HTMLElement;
    http = TestBed.inject(HttpTestingController);
    await stable();
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  async function type(selector: string, value: string): Promise<void> {
    const field = element.querySelector<HTMLInputElement>(selector)!;
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('blur'));
    await stable();
  }

  function submitForm(): void {
    element.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
  }

  it("affiche les erreurs de validation sans appeler l'API si le formulaire est vide", async () => {
    submitForm();
    await stable();

    http.expectNone('/api/auth/login');
    expect(element.textContent).toContain("L'email est obligatoire.");
    expect(element.textContent).toContain('Le mot de passe est obligatoire.');
  });

  it('affiche une erreur pour un email mal formé', async () => {
    await type('#email', 'pas-un-email');
    await type('#password', 'Secret123!');
    submitForm();
    await stable();

    http.expectNone('/api/auth/login');
    expect(element.textContent).toContain('Cet email n’est pas valide.');
  });

  it('connecte et redirige vers l’accueil en cas de succès', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    await type('#email', 'membre@test.fr');
    await type('#password', 'Secret123!');
    submitForm();
    await stable();

    const req = http.expectOne('/api/auth/login');
    expect(req.request.body).toEqual({ email: 'membre@test.fr', password: 'Secret123!' });
    req.flush({ token: 'jwt-de-test', role: 'MEMBER' });
    await stable();

    expect(navigate).toHaveBeenCalledWith(['/']);
    expect(element.querySelector('.server-error')).toBeNull();
  });

  it("affiche le message du back en cas d'échec et ne redirige pas", async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate');
    await type('#email', 'membre@test.fr');
    await type('#password', 'mauvais-mot-de-passe');
    submitForm();
    await stable();

    http
      .expectOne('/api/auth/login')
      .flush('Email ou mot de passe invalide.', { status: 401, statusText: 'Unauthorized' });
    await stable();

    expect(element.textContent).toContain('Email ou mot de passe invalide.');
    expect(navigate).not.toHaveBeenCalled();
  });
});
