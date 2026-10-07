import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { Auth } from '../../../core/auth/auth';
import { Confirmation } from '../../../core/dialog/confirmation';
import { CommentDto, EventDetailResponse } from '../../../domain/event.model';
import { EventDetailPage } from './event-detail-page';

const DETAIL: EventDetailResponse = {
  title: 'Soirée jeux au café ludique',
  description: 'Retrouvez vos amis autour de jeux de société.',
  location: 'Café ludique Les Quatre As, 69001 Lyon',
  startDateTime: '2026-10-16T19:30:00',
  endDateTime: '2026-10-16T23:00:00',
  affiliatedPrice: 3,
  nonAffiliatedPrice: 6,
  maxSeats: 24,
  category: 'LEISURE',
  status: 'PUBLISHED',
  remainingSeats: 24,
  waitingCount: 0,
  owner: false,
  myRegistration: null,
  gallery: [
    { id: 5, eventId: 5, url: 'https://i.ibb.co/a/preview.webp', isPreview: true },
    { id: 35, eventId: 5, url: 'https://i.ibb.co/b/second.webp', isPreview: false },
  ],
};

const COMMENTS: CommentDto[] = [
  {
    id: 1,
    eventId: 5,
    authorDisplayName: 'John D.',
    content: 'Super initiative !',
    createdAt: '2026-09-10T14:00:00',
  },
];

describe('EventDetailPage', () => {
  let http: HttpTestingController;
  let connected: boolean;

  const stable = () => TestBed.inject(ApplicationRef).whenStable();
  const flushPromises = () => new Promise<void>((resolve) => setTimeout(resolve));

  beforeEach(() => {
    connected = false;
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'evenements/:id', component: EventDetailPage }]),
        { provide: Auth, useValue: { isAuthenticated: () => connected } },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function open(url: string): Promise<HTMLElement> {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url, EventDetailPage);
    TestBed.tick();
    return harness.routeNativeElement as HTMLElement;
  }

  async function openLoadedEvent(): Promise<HTMLElement> {
    const element = await open('/evenements/5');
    http.expectOne('/api/events/5').flush(DETAIL);
    http.expectOne('/api/events/5/comments').flush([]);
    await stable();
    return element;
  }

  it('affiche un indicateur de chargement avant la réponse', async () => {
    const element = await open('/evenements/5');

    expect(element.querySelector('app-loading-state')).not.toBeNull();

    http.expectOne('/api/events/5').flush(DETAIL);
    http.expectOne('/api/events/5/comments').flush([]);
  });

  it('affiche le détail, la galerie et les commentaires', async () => {
    const element = await open('/evenements/5');
    http.expectOne('/api/events/5').flush(DETAIL);
    http.expectOne('/api/events/5/comments').flush(COMMENTS);
    await stable();

    expect(element.querySelector('h1')?.textContent).toBe('Soirée jeux au café ludique');
    expect(element.textContent).toContain('16/10/2026 19:30');
    expect(element.textContent).toContain('3 €');
    expect(element.textContent).toContain('6 €');
    expect(element.textContent).toContain('Retrouvez vos amis autour de jeux de société.');
    expect(element.querySelectorAll('app-image-gallery img')).toHaveLength(2);
    expect(element.querySelectorAll('.comment')).toHaveLength(1);
  });

  it('affiche la page introuvable quand le serveur répond 404', async () => {
    const element = await open('/evenements/9999');
    http
      .expectOne('/api/events/9999')
      .flush('introuvable', { status: 404, statusText: 'Not Found' });
    http.expectOne('/api/events/9999/comments').flush([]);
    await stable();

    expect(element.querySelector('app-not-found-page')).not.toBeNull();
    expect(element.querySelector('article')).toBeNull();
  });

  it("affiche la page introuvable sans appeler le serveur quand l'identifiant est invalide", async () => {
    const element = await open('/evenements/abc');
    await stable();

    expect(element.querySelector('app-not-found-page')).not.toBeNull();
    expect(http.match((request) => request.url.startsWith('/api/events/'))).toHaveLength(0);
  });

  it('affiche une erreur avec un bouton Réessayer qui relance la requête', async () => {
    const element = await open('/evenements/5');
    http.expectOne('/api/events/5').flush('boom', { status: 500, statusText: 'Server Error' });
    http.expectOne('/api/events/5/comments').flush([]);
    await stable();

    expect(element.querySelector('app-error-state')).not.toBeNull();

    element.querySelector<HTMLButtonElement>('app-error-state button')!.click();
    TestBed.tick();
    http.expectOne('/api/events/5').flush(DETAIL);
    await stable();

    expect(element.querySelector('app-error-state')).toBeNull();
    expect(element.querySelector('h1')?.textContent).toBe('Soirée jeux au café ludique');
  });

  it('invite un visiteur à se connecter au lieu de proposer le bouton', async () => {
    const element = await openLoadedEvent();

    expect(element.querySelector('app-registration-panel')).toBeNull();
    expect(element.querySelector('.event__login-hint a')?.getAttribute('href')).toBe('/connexion');
  });

  it("propose le bouton S'inscrire à un utilisateur connecté", async () => {
    connected = true;

    const element = await openLoadedEvent();

    expect(element.querySelector('app-registration-panel .registration__button')).not.toBeNull();
    expect(element.querySelector('.event__login-hint')).toBeNull();
  });

  it("inscrit l'utilisateur puis recharge le détail", async () => {
    connected = true;
    const element = await openLoadedEvent();

    element.querySelector<HTMLButtonElement>('.registration__button')!.click();

    const request = http.expectOne('/api/events/5/registrations');
    expect(request.request.method).toBe('POST');
    request.flush(
      {
        id: 1,
        eventId: 5,
        userEmail: 'jean@example.com',
        status: 'REGISTERED',
        registrationDate: '2026-10-05T10:00:00',
      },
      { status: 201, statusText: 'Created' },
    );
    await flushPromises();
    TestBed.tick();

    http
      .expectOne('/api/events/5')
      .flush({ ...DETAIL, myRegistration: { status: 'REGISTERED', waitingPosition: null } });
    await stable();

    expect(element.querySelector('.registration__status')?.textContent).toContain(
      'Vous êtes inscrit',
    );
    expect(element.querySelector('.registration__button')?.textContent?.trim()).toBe(
      'Se désinscrire',
    );
    expect(element.querySelector('h1')?.textContent).toBe('Soirée jeux au café ludique');
  });

  it("affiche le message du back quand l'inscription est refusée", async () => {
    connected = true;
    const element = await openLoadedEvent();

    element.querySelector<HTMLButtonElement>('.registration__button')!.click();

    http.expectOne('/api/events/5/registrations').flush('Vous êtes déjà inscrit à cet événement.', {
      status: 400,
      statusText: 'Bad Request',
    });
    await flushPromises();
    TestBed.tick();
    await stable();

    expect(element.querySelector('.registration__error')?.textContent).toBe(
      'Vous êtes déjà inscrit à cet événement.',
    );
  });
  it("désinscrit l'utilisateur après confirmation puis recharge le détail", async () => {
    connected = true;

    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const element = await open('/evenements/5');

    http.expectOne('/api/events/5').flush({
      ...DETAIL,
      myRegistration: {
        status: 'REGISTERED',
        waitingPosition: null,
      },
    });
    http.expectOne('/api/events/5/comments').flush([]);

    await stable();

    expect(element.querySelector('.registration__button')?.textContent?.trim()).toBe(
      'Se désinscrire',
    );

    element.querySelector<HTMLButtonElement>('.registration__button')!.click();

    await flushPromises();
    TestBed.tick();

    const request = http.expectOne('/api/events/5/registrations/me');

    expect(request.request.method).toBe('DELETE');

    request.flush(null, {
      status: 204,
      statusText: 'No Content',
    });

    await flushPromises();
    TestBed.tick();

    http.expectOne('/api/events/5').flush({
      ...DETAIL,
      myRegistration: null,
    });

    await stable();

    expect(element.querySelector('.registration__button')?.textContent?.trim()).toBe("S'inscrire");
  });

  it("ne désinscrit pas l'utilisateur si la confirmation est annulée", async () => {
    connected = true;

    const confirm = vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(false);

    const element = await open('/evenements/5');

    http.expectOne('/api/events/5').flush({
      ...DETAIL,
      myRegistration: {
        status: 'REGISTERED',
        waitingPosition: null,
      },
    });
    http.expectOne('/api/events/5/comments').flush([]);

    await stable();

    element.querySelector<HTMLButtonElement>('.registration__button')!.click();

    await flushPromises();
    TestBed.tick();

    expect(confirm).toHaveBeenCalledWith(
      'Se désinscrire de « Soirée jeux au café ludique » ?',
      'Se désinscrire',
      'Annuler',
    );

    http.expectNone('/api/events/5/registrations/me');
  });

  it('affiche le message du back quand la désinscription est refusée', async () => {
    connected = true;

    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const element = await open('/evenements/5');

    http.expectOne('/api/events/5').flush({
      ...DETAIL,
      myRegistration: {
        status: 'REGISTERED',
        waitingPosition: null,
      },
    });
    http.expectOne('/api/events/5/comments').flush([]);

    await stable();

    element.querySelector<HTMLButtonElement>('.registration__button')!.click();

    await flushPromises();
    TestBed.tick();

    http
      .expectOne('/api/events/5/registrations/me')
      .flush("Vous n'êtes pas inscrit à cet événement.", {
        status: 400,
        statusText: 'Bad Request',
      });

    await flushPromises();
    TestBed.tick();
    await stable();

    expect(element.querySelector('.registration__error')?.textContent).toBe(
      "Vous n'êtes pas inscrit à cet événement.",
    );
  });

  it('télécharge la fiche PDF au clic sur le bouton', async () => {
    const originalCreate = URL.createObjectURL;
    const originalRevoke = URL.revokeObjectURL;
    URL.createObjectURL = vi.fn(() => 'blob:fiche');
    URL.revokeObjectURL = vi.fn();
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});

    try {
      const element = await openLoadedEvent();

      element.querySelector<HTMLButtonElement>('.event__pdf-button')!.click();

      const request = http.expectOne('/api/events/5/pdf');
      expect(request.request.method).toBe('GET');
      expect(request.request.responseType).toBe('blob');
      request.flush(new Blob(['pdf'], { type: 'application/pdf' }));
      await flushPromises();

      expect(click).toHaveBeenCalledTimes(1);
      const link = click.mock.contexts[0] as HTMLAnchorElement;
      expect(link.download).toBe('evenement-5.pdf');
    } finally {
      URL.createObjectURL = originalCreate;
      URL.revokeObjectURL = originalRevoke;
      vi.restoreAllMocks();
    }
  });

  it('affiche un message quand le téléchargement du PDF échoue', async () => {
    const element = await openLoadedEvent();

    element.querySelector<HTMLButtonElement>('.event__pdf-button')!.click();

    http
      .expectOne('/api/events/5/pdf')
      .flush(new Blob(['erreur']), { status: 500, statusText: 'Server Error' });
    await flushPromises();
    TestBed.tick();
    await stable();

    expect(element.querySelector('.event__pdf-error')?.textContent).toContain(
      'Le téléchargement de la fiche PDF a échoué',
    );
  });

  it("n'affiche pas le formulaire de commentaire à un visiteur", async () => {
    const element = await openLoadedEvent();

    expect(element.querySelector('app-comment-form')).toBeNull();
  });

  it('publie un commentaire puis recharge la liste', async () => {
    connected = true;
    const element = await openLoadedEvent();

    const textarea = element.querySelector('app-comment-form textarea') as HTMLTextAreaElement;

    textarea.value = 'Super événement !';
    textarea.dispatchEvent(new Event('input'));
    TestBed.tick();

    element.querySelector<HTMLFormElement>('app-comment-form form')!.dispatchEvent(
      new Event('submit', {
        bubbles: true,
        cancelable: true,
      }),
    );

    const request = http.expectOne('/api/comments');

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      eventId: 5,
      content: 'Super événement !',
    });

    request.flush(
      {
        id: 2,
        eventId: 5,
        authorDisplayName: 'Jean D.',
        content: 'Super événement !',
        createdAt: '2026-10-06T09:50:00',
      },
      { status: 201, statusText: 'Created' },
    );

    await flushPromises();
    TestBed.tick();

    http.expectOne('/api/events/5/comments').flush([
      {
        id: 2,
        eventId: 5,
        authorDisplayName: 'Jean D.',
        content: 'Super événement !',
        createdAt: '2026-10-06T09:50:00',
      },
    ]);

    await stable();

    expect((element.querySelector('app-comment-form textarea') as HTMLTextAreaElement).value).toBe(
      '',
    );
    expect(element.textContent).toContain('Super événement !');
  });

  it('affiche le message du back quand le commentaire est refusé', async () => {
    connected = true;
    const element = await openLoadedEvent();

    const textarea = element.querySelector('app-comment-form textarea') as HTMLTextAreaElement;

    textarea.value = 'Mon commentaire';
    textarea.dispatchEvent(new Event('input'));
    TestBed.tick();

    element.querySelector<HTMLFormElement>('app-comment-form form')!.dispatchEvent(
      new Event('submit', {
        bubbles: true,
        cancelable: true,
      }),
    );

    http
      .expectOne('/api/comments')
      .flush('Votre compte doit être actif pour publier un commentaire.', {
        status: 400,
        statusText: 'Bad Request',
      });

    await flushPromises();
    TestBed.tick();
    await stable();

    expect(element.querySelector('[role="alert"]')?.textContent).toContain(
      'Votre compte doit être actif pour publier un commentaire.',
    );
  });

  describe('actions du propriétaire', () => {
    async function openOwnedEvent(overrides: Partial<EventDetailResponse> = {}) {
      connected = true;
      const element = await open('/evenements/5');
      http.expectOne('/api/events/5').flush({ ...DETAIL, owner: true, ...overrides });
      http.expectOne('/api/events/5/comments').flush([]);
      await stable();
      return element;
    }

    const ownerButtons = (element: HTMLElement) =>
      Array.from(
        element.querySelectorAll<HTMLButtonElement>('app-owner-actions .owner-actions__button'),
      );

    const labels = (element: HTMLElement) =>
      ownerButtons(element).map((button) => button.textContent?.trim());

    async function clickOwnerButton(element: HTMLElement, label: string) {
      ownerButtons(element)
        .find((button) => button.textContent?.trim() === label)!
        .click();
      await flushPromises();
      TestBed.tick();
    }

    async function reloadWith(detail: EventDetailResponse) {
      await flushPromises();
      TestBed.tick();
      http.expectOne('/api/events/5').flush(detail);
      await stable();
    }

    it("n'affiche pas les actions de gestion à un utilisateur qui n'est pas propriétaire", async () => {
      const element = await openOwnedEvent({ owner: false });

      expect(element.querySelector('app-owner-actions')).toBeNull();
    });

    it('affiche les actions de gestion au propriétaire', async () => {
      const element = await openOwnedEvent({ status: 'DRAFT' });

      expect(labels(element)).toEqual(['Modifier', 'Publier', 'Supprimer']);
    });

    it('ouvre le formulaire de modification au clic sur Modifier', async () => {
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const element = await openOwnedEvent();

      await clickOwnerButton(element, 'Modifier');

      expect(navigate).toHaveBeenCalledWith(['/evenements', 5, 'modifier']);
    });

    it("publie l'événement après confirmation puis recharge le détail", async () => {
      const confirm = vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const element = await openOwnedEvent({ status: 'DRAFT' });

      await clickOwnerButton(element, 'Publier');

      expect(confirm).toHaveBeenCalledWith(
        "Publier l'évènement « Soirée jeux au café ludique » ? Il sera visible par tous les utilisateurs.",
        'Publier',
        'Annuler',
      );

      const request = http.expectOne('/api/events/5/publish');
      expect(request.request.method).toBe('POST');
      request.flush({});

      await reloadWith({ ...DETAIL, owner: true, status: 'PUBLISHED' });

      expect(labels(element)).toEqual(['Modifier', 'Terminer', 'Supprimer']);
    });

    it("ne publie pas l'événement si la confirmation est annulée", async () => {
      vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(false);
      const element = await openOwnedEvent({ status: 'DRAFT' });

      await clickOwnerButton(element, 'Publier');

      http.expectNone('/api/events/5/publish');
    });

    it("termine l'événement après confirmation puis recharge le détail", async () => {
      vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const element = await openOwnedEvent({ status: 'PUBLISHED' });

      await clickOwnerButton(element, 'Terminer');

      const request = http.expectOne('/api/events/5/status');
      expect(request.request.method).toBe('POST');
      request.flush({});

      await reloadWith({ ...DETAIL, owner: true, status: 'FINISHED' });

      expect(labels(element)).toEqual(['Modifier']);
    });

    it("annule l'événement après confirmation puis retourne sur Mes évènements", async () => {
      const confirm = vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const element = await openOwnedEvent({ status: 'PUBLISHED', remainingSeats: 20 });

      await clickOwnerButton(element, "Annuler l'événement");

      expect(confirm).toHaveBeenCalledWith(
        "Annuler l'évènement « Soirée jeux au café ludique » ? Les inscrits en seront informés par email.",
        "Oui, annuler l'évènement",
        'Non, revenir',
      );

      const request = http.expectOne('/api/events/5/cancel');
      expect(request.request.method).toBe('POST');
      request.flush({});
      await flushPromises();

      expect(navigate).toHaveBeenCalledWith(['/mes-evenements']);
    });

    it("supprime l'événement après confirmation puis retourne sur Mes évènements", async () => {
      vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const element = await openOwnedEvent({ status: 'DRAFT' });

      await clickOwnerButton(element, 'Supprimer');

      const request = http.expectOne('/api/events/5');
      expect(request.request.method).toBe('DELETE');
      request.flush(null, { status: 204, statusText: 'No Content' });
      await flushPromises();

      expect(navigate).toHaveBeenCalledWith(['/mes-evenements']);
    });

    it("affiche le message du back quand l'action est refusée", async () => {
      vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const element = await openOwnedEvent({ status: 'DRAFT' });

      await clickOwnerButton(element, 'Publier');

      http.expectOne('/api/events/5/publish').flush('Cet événement ne peut pas être publié.', {
        status: 400,
        statusText: 'Bad Request',
      });
      await flushPromises();
      TestBed.tick();
      await stable();

      expect(element.querySelector('.owner-actions__error')?.textContent).toBe(
        'Cet événement ne peut pas être publié.',
      );
    });

    it("affiche un message générique quand l'action échoue sans message du back", async () => {
      vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const element = await openOwnedEvent({ status: 'DRAFT' });

      await clickOwnerButton(element, 'Publier');

      http
        .expectOne('/api/events/5/publish')
        .flush(null, { status: 500, statusText: 'Server Error' });
      await flushPromises();
      TestBed.tick();
      await stable();

      expect(element.querySelector('.owner-actions__error')?.textContent).toBe(
        "L'action a échoué, réessayez.",
      );
    });
  });
});
