import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Confirmation } from '../../../core/dialog/confirmation';
import { OrganizerEventDto } from '../../../domain/event.model';
import { MyEventsPage } from './my-events-page';

function makeEvent(overrides: Partial<OrganizerEventDto> = {}): OrganizerEventDto {
  return {
    id: 1,
    title: 'Concert',
    category: 'CULTURE',
    startDateTime: '2026-11-20T20:00:00',
    endDateTime: null,
    status: 'DRAFT',
    maxSeats: 100,
    registeredCount: 0,
    ...overrides,
  };
}

describe('MyEventsPage', () => {
  let http: HttpTestingController;

  const stable = () => TestBed.inject(ApplicationRef).whenStable();
  const macrotask = () => new Promise((resolve) => setTimeout(resolve));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function render(events: OrganizerEventDto[]): Promise<HTMLElement> {
    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush(events);
    await stable();
    return fixture.nativeElement as HTMLElement;
  }

  it('affiche un indicateur de chargement avant la réponse', () => {
    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-loading-state')).not.toBeNull();

    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([]);
  });

  it("affiche un état vide quand l'organisateur n'a créé aucun évènement", async () => {
    const element = await render([]);

    expect(element.querySelector('app-empty-state')).not.toBeNull();
  });

  it('affiche les évènements avec les badges de catégorie et de statut', async () => {
    const element = await render([makeEvent()]);

    expect(element.textContent).toContain('Concert');
    expect(element.querySelector('app-category-badge')?.textContent?.trim()).toBe('Culture');
    expect(element.querySelector('app-event-status-badge')?.textContent?.trim()).toBe('Brouillon');
  });

  it('trie par date au clic sur l’en-tête, et inverse au second clic', async () => {
    const element = await render([
      makeEvent({ id: 1, title: 'Plus tôt', startDateTime: '2026-11-01T10:00:00' }),
      makeEvent({ id: 2, title: 'Plus tard', startDateTime: '2026-12-01T10:00:00' }),
    ]);
    const dateHeaderButton = Array.from(element.querySelectorAll('th button')).find((button) =>
      button.textContent?.includes('Dates'),
    ) as HTMLButtonElement;

    dateHeaderButton.click();
    await stable();

    let rows = element.querySelectorAll('tbody tr');
    expect(rows[0].textContent).toContain('Plus tôt');
    expect(rows[1].textContent).toContain('Plus tard');

    dateHeaderButton.click();
    await stable();

    rows = element.querySelectorAll('tbody tr');
    expect(rows[0].textContent).toContain('Plus tard');
    expect(rows[1].textContent).toContain('Plus tôt');
  });

  describe('actions selon le statut', () => {
    it('brouillon : Publier (bouton) ; Modifier, Supprimer et Voir les inscriptions (liens)', async () => {
      const element = await render([makeEvent()]);

      expect(element.querySelectorAll('app-action-button button')).toHaveLength(1);
      expect(element.querySelector('button[aria-label="Publier"]')).not.toBeNull();
      expect(element.querySelectorAll('app-action-button a')).toHaveLength(3);
    });

    it('brouillon : Supprimer mène à la page de suppression', async () => {
      const element = await render([makeEvent({ id: 7 })]);

      const link = element.querySelector<HTMLAnchorElement>('a[aria-label="Supprimer"]')!;
      expect(link.getAttribute('href')).toBe('/mes-evenements/7/supprimer');
    });

    it('publié sans inscrit : Supprimer (lien), pas Annuler', async () => {
      const element = await render([makeEvent({ id: 7, status: 'PUBLISHED', registeredCount: 0 })]);

      expect(element.querySelector('a[aria-label="Supprimer"]')?.getAttribute('href')).toBe(
        '/mes-evenements/7/supprimer',
      );
      expect(element.querySelector('a[aria-label="Annuler"]')).toBeNull();
    });

    it('publié avec inscrits : Annuler mène à la page d’annulation, pas Supprimer', async () => {
      const element = await render([makeEvent({ id: 7, status: 'PUBLISHED', registeredCount: 3 })]);

      expect(element.querySelector('a[aria-label="Annuler"]')?.getAttribute('href')).toBe(
        '/mes-evenements/7/annuler',
      );
      expect(element.querySelector('a[aria-label="Supprimer"]')).toBeNull();
      expect(element.querySelector('button[aria-label="Terminer"]')).not.toBeNull();
    });

    it('terminé ou annulé : ni suppression ni annulation', async () => {
      const element = await render([
        makeEvent({ id: 1, status: 'FINISHED' }),
        makeEvent({ id: 2, status: 'CANCELLED' }),
      ]);

      expect(element.querySelector('a[aria-label="Supprimer"]')).toBeNull();
      expect(element.querySelector('a[aria-label="Annuler"]')).toBeNull();
    });
  });

  describe('publier', () => {
    it('publie après confirmation et recharge la liste', async () => {
      const confirm = vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const element = await render([makeEvent()]);

      element.querySelector<HTMLButtonElement>('button[aria-label="Publier"]')!.click();
      await stable();

      http
        .expectOne((req) => req.method === 'POST' && req.url.endsWith('/events/1/publish'))
        .flush({});
      await macrotask();
      TestBed.tick();
      http
        .expectOne((req) => req.url.endsWith('/events/mine'))
        .flush([makeEvent({ status: 'PUBLISHED' })]);
      await stable();

      expect(confirm).toHaveBeenCalledWith(expect.stringContaining('Concert'), 'Publier', 'Annuler');
      expect(element.querySelector('app-event-status-badge')?.textContent?.trim()).toBe('Publié');
    });

    it('ne publie pas si on annule la confirmation', async () => {
      vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(false);
      const element = await render([makeEvent()]);

      element.querySelector<HTMLButtonElement>('button[aria-label="Publier"]')!.click();
      await stable();

      expect(element.querySelector('app-event-status-badge')?.textContent?.trim()).toBe('Brouillon');
    });

    it("affiche le message du back si la publication n'est pas permise", async () => {
      vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const element = await render([makeEvent()]);

      element.querySelector<HTMLButtonElement>('button[aria-label="Publier"]')!.click();
      await stable();

      http
        .expectOne((req) => req.method === 'POST' && req.url.endsWith('/events/1/publish'))
        .flush('Cet événement ne peut pas être publié dans son état actuel.', {
          status: 400,
          statusText: 'Bad Request',
        });
      await macrotask();
      await stable();

      expect(element.textContent).toContain(
        'Cet événement ne peut pas être publié dans son état actuel.',
      );
    });

    it("affiche un message générique quand l'erreur ne contient pas de message exploitable", async () => {
      vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const element = await render([makeEvent()]);

      element.querySelector<HTMLButtonElement>('button[aria-label="Publier"]')!.click();
      await stable();

      http
        .expectOne((req) => req.method === 'POST' && req.url.endsWith('/events/1/publish'))
        .flush(null, { status: 500, statusText: 'Server Error' });
      await macrotask();
      await stable();

      expect(element.textContent).toContain("L'action a échoué, réessayez.");
    });
  });

  describe('terminer', () => {
    it('termine après confirmation et recharge la liste', async () => {
      const confirm = vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const element = await render([makeEvent({ status: 'PUBLISHED' })]);

      element.querySelector<HTMLButtonElement>('button[aria-label="Terminer"]')!.click();
      await stable();

      http
        .expectOne((req) => req.method === 'POST' && req.url.endsWith('/events/1/status'))
        .flush({});
      await macrotask();
      TestBed.tick();
      http
        .expectOne((req) => req.url.endsWith('/events/mine'))
        .flush([makeEvent({ status: 'FINISHED' })]);
      await stable();

      expect(confirm).toHaveBeenCalledWith(
        expect.stringContaining('Concert'),
        'Terminer',
        'Annuler',
      );
      expect(element.querySelector('app-event-status-badge')?.textContent?.trim()).toBe('Terminé');
    });

    it('ne termine pas si on annule la confirmation', async () => {
      vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(false);
      const element = await render([makeEvent({ status: 'PUBLISHED' })]);

      element.querySelector<HTMLButtonElement>('button[aria-label="Terminer"]')!.click();
      await stable();

      expect(element.querySelector('app-event-status-badge')?.textContent?.trim()).toBe('Publié');
    });

    it("affiche le message du back si le changement de statut n'est pas autorisé", async () => {
      vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);
      const element = await render([makeEvent({ status: 'PUBLISHED' })]);

      element.querySelector<HTMLButtonElement>('button[aria-label="Terminer"]')!.click();
      await stable();

      http
        .expectOne((req) => req.method === 'POST' && req.url.endsWith('/events/1/status'))
        .flush('Ce changement de statut n’est pas autorisé.', {
          status: 400,
          statusText: 'Bad Request',
        });
      await macrotask();
      await stable();

      expect(element.textContent).toContain('Ce changement de statut n’est pas autorisé.');
    });
  });

  describe('affichage sur petit écran', () => {
    it('propose des boutons de tri par catégorie, dates et statut', async () => {
      const element = await render([makeEvent()]);

      const labels = Array.from(element.querySelectorAll('.sort-bar button')).map((button) =>
        button.textContent?.trim(),
      );

      expect(labels).toEqual(['Catégorie', 'Dates', 'Statut']);
    });

    it('trie par date au clic sur le bouton de tri, et inverse au second clic', async () => {
      const element = await render([
        makeEvent({ id: 1, title: 'Plus tôt', startDateTime: '2026-11-01T10:00:00' }),
        makeEvent({ id: 2, title: 'Plus tard', startDateTime: '2026-12-01T10:00:00' }),
      ]);
      const datesButton = Array.from(
        element.querySelectorAll<HTMLButtonElement>('.sort-bar button'),
      ).find((button) => button.textContent?.includes('Dates'))!;

      datesButton.click();
      await stable();

      let rows = element.querySelectorAll('tbody tr');
      expect(rows[0].textContent).toContain('Plus tôt');
      expect(datesButton.classList.contains('active')).toBe(true);
      expect(datesButton.textContent).toContain('▲');

      datesButton.click();
      await stable();

      rows = element.querySelectorAll('tbody tr');
      expect(rows[0].textContent).toContain('Plus tard');
      expect(datesButton.textContent).toContain('▼');
    });

    it('reprend la catégorie, le statut et les places sous le titre', async () => {
      const element = await render([makeEvent({ registeredCount: 3, maxSeats: 100 })]);

      const meta = element.querySelector('tbody td .event-meta')!;

      expect(meta.querySelector('app-category-badge')?.textContent?.trim()).toBe('Culture');
      expect(meta.querySelector('app-event-status-badge')?.textContent?.trim()).toBe('Brouillon');
      expect(meta.querySelector('.event-places')?.textContent?.trim()).toBe('3 / 100 places');
    });
  });
});
