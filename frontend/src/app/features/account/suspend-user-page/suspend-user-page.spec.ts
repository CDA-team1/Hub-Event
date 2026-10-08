import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { toIsoDate } from '../../../domain/calendar-rules';
import { AdminUserDto } from '../../../domain/account.model';
import { SuspendUserPage } from './suspend-user-page';

function makeUser(): AdminUserDto {
  return {
    id: 7,
    lastName: 'Martin',
    firstName: 'Alice',
    postalAddress: '1 rue de Test',
    email: 'alice@test.com',
    phone: '0600000000',
    status: 'ACTIVE',
    role: 'MEMBER',
    clubs: [],
  };
}

describe('SuspendUserPage', () => {
  let http: HttpTestingController;

  const stable = async () => {
    await new Promise((resolve) => setTimeout(resolve));
    await TestBed.inject(ApplicationRef).whenStable();
  };

  function configure(id: string) {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id }) } },
        },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  }

  async function open(id = '7'): Promise<HTMLElement> {
    configure(id);
    const fixture = TestBed.createComponent(SuspendUserPage);
    http
      .expectOne((req) => req.method === 'GET' && req.url.endsWith(`/admin/users/${id}`))
      .flush(makeUser());
    await stable();
    return fixture.nativeElement as HTMLElement;
  }

  async function type(element: HTMLElement, selector: string, value: string): Promise<void> {
    const field = element.querySelector<HTMLInputElement | HTMLTextAreaElement>(selector)!;
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('change'));
    field.dispatchEvent(new Event('blur'));
    await stable();
  }

  function submitForm(element: HTMLElement): void {
    element.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
  }

  afterEach(() => http.verify());

  it('affiche un indicateur de chargement avant la réponse', () => {
    configure('7');
    const fixture = TestBed.createComponent(SuspendUserPage);
    TestBed.tick();

    expect((fixture.nativeElement as HTMLElement).querySelector('app-loading-state')).not.toBeNull();

    http.expectOne((req) => req.url.endsWith('/admin/users/7')).flush(makeUser());
  });

  it('affiche le compte concerné et le formulaire une fois chargé', async () => {
    const element = await open();

    expect(element.textContent).toContain('Alice Martin');
    expect(element.textContent).toContain('alice@test.com');
    expect(element.querySelector('#reason')).not.toBeNull();
    expect(element.querySelector('#endDate')).not.toBeNull();
  });

  it("affiche une erreur sans appeler le serveur quand l'identifiant est invalide", async () => {
    configure('abc');
    const fixture = TestBed.createComponent(SuspendUserPage);
    TestBed.tick();
    await stable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Compte introuvable.');
  });

  it('affiche une erreur si le compte ne se charge pas', async () => {
    configure('7');
    const fixture = TestBed.createComponent(SuspendUserPage);
    http
      .expectOne((req) => req.url.endsWith('/admin/users/7'))
      .flush(null, { status: 500, statusText: 'Server Error' });
    await stable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Impossible de charger le compte.',
    );
  });

  it("n'envoie rien et réclame le motif quand il est vide", async () => {
    const element = await open();

    submitForm(element);
    await stable();

    expect(element.textContent).toContain('Veuillez renseigner le motif de la suspension.');
  });

  it('suspend définitivement quand la date de fin est vide, puis revient à la liste', async () => {
    const element = await open();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    await type(element, '#reason', 'Propos injurieux');
    submitForm(element);
    await stable();

    const request = http.expectOne(
      (req) => req.method === 'POST' && req.url.endsWith('/admin/users/7/suspension'),
    );
    expect(request.request.body).toEqual({ reason: 'Propos injurieux', endDate: null });
    request.flush(null);
    await stable();

    expect(navigate).toHaveBeenCalledWith(['/admin/comptes']);
  });

  it('suspend temporairement quand une date de fin postérieure à aujourd’hui est saisie', async () => {
    const element = await open();

    await type(element, '#reason', 'Propos injurieux');
    await type(element, '#endDate', '2099-12-31');
    submitForm(element);
    await stable();

    const request = http.expectOne(
      (req) => req.method === 'POST' && req.url.endsWith('/admin/users/7/suspension'),
    );
    expect(request.request.body).toEqual({ reason: 'Propos injurieux', endDate: '2099-12-31' });
    request.flush(null);
    await stable();
  });

  it("refuse une date de fin passée ou égale à aujourd'hui, sans appel réseau", async () => {
    const element = await open();

    await type(element, '#reason', 'Propos injurieux');
    await type(element, '#endDate', toIsoDate(new Date()));
    submitForm(element);
    await stable();

    expect(element.textContent).toContain('La date de fin doit être postérieure à aujourd’hui.');
  });

  it('affiche le message du back et ne redirige pas si la suspension est refusée', async () => {
    const element = await open();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    await type(element, '#reason', 'Propos injurieux');
    submitForm(element);
    await stable();

    http
      .expectOne((req) => req.method === 'POST' && req.url.endsWith('/admin/users/7/suspension'))
      .flush('Le motif de la suspension est obligatoire.', {
        status: 400,
        statusText: 'Bad Request',
      });
    await stable();

    expect(element.textContent).toContain('Le motif de la suspension est obligatoire.');
    expect(navigate).not.toHaveBeenCalled();
  });

  it('affiche un message générique quand l’erreur ne contient pas de message exploitable', async () => {
    const element = await open();

    await type(element, '#reason', 'Propos injurieux');
    submitForm(element);
    await stable();

    http
      .expectOne((req) => req.method === 'POST' && req.url.endsWith('/admin/users/7/suspension'))
      .flush(null, { status: 500, statusText: 'Server Error' });
    await stable();

    expect(element.textContent).toContain('La suspension a échoué, réessayez.');
  });

  it('revient à la liste sans appel réseau au clic sur Annuler', async () => {
    const element = await open();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    element.querySelector<HTMLButtonElement>('button[type="button"]')!.click();
    await stable();

    expect(navigate).toHaveBeenCalledWith(['/admin/comptes']);
  });
});
