import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { RegistrationDto } from '../../../domain/event.model';
import { EventRegistrationsPage } from './event-registrations-page';

function makeRegistration(overrides: Partial<RegistrationDto> = {}): RegistrationDto {
  return {
    id: 1,
    eventId: 5,
    userId: 20,
    userEmail: 'jean@example.com',
    status: 'REGISTERED',
    registrationDate: '2026-10-01T10:00:00',
    ...overrides,
  };
}

describe('EventRegistrationsPage', () => {
  let http: HttpTestingController;

  const stable = () => TestBed.inject(ApplicationRef).whenStable();
  const macrotask = () => new Promise((resolve) => setTimeout(resolve));

  function typeReason(element: HTMLElement, value: string): void {
    const textarea = element.querySelector<HTMLTextAreaElement>('#reason')!;
    textarea.value = value;
    textarea.dispatchEvent(new Event('change'));
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '5' }) } },
        },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('affiche un indicateur de chargement avant la réponse', () => {
    const fixture = TestBed.createComponent(EventRegistrationsPage);
    TestBed.tick();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-loading-state')).not.toBeNull();

    http.expectOne((req) => req.url.endsWith('/events/5/registrations')).flush([]);
  });

  it("affiche un état vide quand l'évènement n'a aucune inscription", async () => {
    const fixture = TestBed.createComponent(EventRegistrationsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/5/registrations')).flush([]);
    await stable();

    expect((fixture.nativeElement as HTMLElement).querySelector('app-empty-state')).not.toBeNull();
  });

  it('affiche les inscrits avec leur statut', async () => {
    const fixture = TestBed.createComponent(EventRegistrationsPage);
    TestBed.tick();
    http
      .expectOne((req) => req.url.endsWith('/events/5/registrations'))
      .flush([
        makeRegistration(),
        makeRegistration({ id: 2, userId: 21, userEmail: 'attente@example.com', status: 'WAITING_LIST' }),
      ]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('jean@example.com');
    expect(element.textContent).toContain('Inscrit');
    expect(element.textContent).toContain('attente@example.com');
    expect(element.textContent).toContain('Liste d’attente');
  });

  it('affiche le champ Motif au clic sur Désinscrire, masqué au départ', async () => {
    const fixture = TestBed.createComponent(EventRegistrationsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/5/registrations')).flush([makeRegistration()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.cancel-form')).toBeNull();

    element.querySelector<HTMLButtonElement>('app-action-button button')!.click();
    await stable();

    expect(element.querySelector('.cancel-form')).not.toBeNull();
    expect(element.textContent).toContain('jean@example.com');
  });

  it("refuse l'envoi sans motif", async () => {
    const fixture = TestBed.createComponent(EventRegistrationsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/5/registrations')).flush([makeRegistration()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('app-action-button button')!.click();
    await stable();

    element.querySelector<HTMLButtonElement>('.cancel-form__actions button')!.click();
    await stable();

    expect(element.textContent).toContain('Le motif est obligatoire.');
  });

  it('masque le formulaire sans appel réseau au clic sur Annuler', async () => {
    const fixture = TestBed.createComponent(EventRegistrationsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/5/registrations')).flush([makeRegistration()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('app-action-button button')!.click();
    await stable();

    element.querySelectorAll<HTMLButtonElement>('.cancel-form__actions button')[1].click();
    await stable();

    expect(element.querySelector('.cancel-form')).toBeNull();
  });

  it('désinscrit le membre avec le motif saisi et recharge la liste', async () => {
    const fixture = TestBed.createComponent(EventRegistrationsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/5/registrations')).flush([makeRegistration()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('app-action-button button')!.click();
    await stable();

    typeReason(element, 'Comportement inapproprié');
    element.querySelector<HTMLButtonElement>('.cancel-form__actions button')!.click();
    await stable();

    const request = http.expectOne(
      (req) => req.method === 'DELETE' && req.url.endsWith('/events/5/registrations/20'),
    );
    expect(request.request.body).toEqual({ reason: 'Comportement inapproprié' });
    request.flush(null);
    await macrotask();
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/5/registrations')).flush([]);
    await stable();

    expect(element.querySelector('.cancel-form')).toBeNull();
    expect(element.querySelector('app-empty-state')).not.toBeNull();
  });

  it("affiche une erreur si la désinscription échoue", async () => {
    const fixture = TestBed.createComponent(EventRegistrationsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/5/registrations')).flush([makeRegistration()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('app-action-button button')!.click();
    await stable();

    typeReason(element, 'Comportement inapproprié');
    element.querySelector<HTMLButtonElement>('.cancel-form__actions button')!.click();
    await stable();

    http
      .expectOne((req) => req.method === 'DELETE' && req.url.endsWith('/events/5/registrations/20'))
      .flush('Ce membre n’est pas inscrit à cet événement', { status: 400, statusText: 'Bad Request' });
    await macrotask();
    await stable();

    expect(element.textContent).toContain('La désinscription a échoué, réessayez.');
  });
});
