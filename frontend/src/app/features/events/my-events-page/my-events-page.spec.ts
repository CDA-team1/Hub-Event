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

  it('affiche un indicateur de chargement avant la réponse', () => {
    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-loading-state')).not.toBeNull();

    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([]);
  });

  it("affiche un état vide quand l'organisateur n'a créé aucun évènement", async () => {
    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([]);
    await stable();

    expect((fixture.nativeElement as HTMLElement).querySelector('app-empty-state')).not.toBeNull();
  });

  it('affiche les évènements avec les badges de catégorie et de statut', async () => {
    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([makeEvent()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Concert');
    expect(element.querySelector('app-category-badge')?.textContent?.trim()).toBe('Culture');
    expect(element.querySelector('app-event-status-badge')?.textContent?.trim()).toBe('Brouillon');
  });

  it('trie par date au clic sur l’en-tête, et inverse au second clic', async () => {
    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http
      .expectOne((req) => req.url.endsWith('/events/mine'))
      .flush([
        makeEvent({ id: 1, title: 'Plus tôt', startDateTime: '2026-11-01T10:00:00' }),
        makeEvent({ id: 2, title: 'Plus tard', startDateTime: '2026-12-01T10:00:00' }),
      ]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
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

  it('propose Publier et Supprimer pour un évènement en brouillon', async () => {
    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([makeEvent()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;

    // Publier, Supprimer : deux boutons. Modifier et Voir les inscriptions restent deux liens,
    // affichés pour tout évènement quel que soit son statut.
    expect(element.querySelectorAll('app-action-button button')).toHaveLength(2);
    expect(element.querySelectorAll('app-action-button a')).toHaveLength(2);
  });

  it('publie un évènement en brouillon après confirmation et recharge la liste', async () => {
    const confirm = vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([makeEvent()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('app-action-button button')[0].click();
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

  it("ne publie pas si on annule la confirmation", async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(false);

    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([makeEvent()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('app-action-button button')[0].click();
    await stable();

    expect(element.querySelector('app-event-status-badge')?.textContent?.trim()).toBe('Brouillon');
  });

  it("affiche le message du back si la publication n'est pas permise", async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([makeEvent()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('app-action-button button')[0].click();
    await stable();

    http
      .expectOne((req) => req.method === 'POST' && req.url.endsWith('/events/1/publish'))
      .flush('Cet événement ne peut pas être publié dans son état actuel.', {
        status: 400,
        statusText: 'Bad Request',
      });
    await macrotask();
    await stable();

    expect(element.textContent).toContain('Cet événement ne peut pas être publié dans son état actuel.');
  });

  it('termine un évènement publié après confirmation et recharge la liste', async () => {
    const confirm = vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http
      .expectOne((req) => req.url.endsWith('/events/mine'))
      .flush([makeEvent({ status: 'PUBLISHED' })]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('app-action-button button')[0].click();
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

    expect(confirm).toHaveBeenCalledWith(expect.stringContaining('Concert'), 'Terminer', 'Annuler');
    expect(element.querySelector('app-event-status-badge')?.textContent?.trim()).toBe('Terminé');
  });

  it("ne termine pas si on annule la confirmation", async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(false);

    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http
      .expectOne((req) => req.url.endsWith('/events/mine'))
      .flush([makeEvent({ status: 'PUBLISHED' })]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('app-action-button button')[0].click();
    await stable();

    expect(element.querySelector('app-event-status-badge')?.textContent?.trim()).toBe('Publié');
  });

  it("affiche le message du back si le changement de statut n'est pas autorisé", async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http
      .expectOne((req) => req.url.endsWith('/events/mine'))
      .flush([makeEvent({ status: 'PUBLISHED' })]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('app-action-button button')[0].click();
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

  it('propose Annuler (pas Supprimer) pour un évènement publié avec des inscrits', async () => {
    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http
      .expectOne((req) => req.url.endsWith('/events/mine'))
      .flush([makeEvent({ status: 'PUBLISHED', registeredCount: 3 })]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;

    // Terminer, Annuler : deux boutons. Modifier et Voir les inscriptions restent deux liens.
    expect(element.querySelectorAll('app-action-button button')).toHaveLength(2);
    expect(element.querySelectorAll('app-action-button a')).toHaveLength(2);
  });

  it('supprime un évènement après confirmation et recharge la liste', async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([makeEvent()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('app-action-button button')[1].click();
    await stable();

    http.expectOne((req) => req.method === 'DELETE' && req.url.endsWith('/events/1')).flush(null);
    await macrotask();
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([]);
    await stable();

    expect(element.querySelector('app-empty-state')).not.toBeNull();
  });

  it("n'appelle pas l'API si on annule la confirmation de suppression", async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(false);

    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([makeEvent()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('app-action-button button')[1].click();
    await stable();

    expect(element.textContent).toContain('Concert');
  });

  it("affiche le message du back quand l'action est refusée", async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([makeEvent()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('app-action-button button')[1].click();
    await stable();

    http
      .expectOne((req) => req.method === 'DELETE' && req.url.endsWith('/events/1'))
      .flush('Cet évènement ne peut pas être supprimé.', { status: 400, statusText: 'Bad Request' });
    await macrotask();
    await stable();

    expect(element.textContent).toContain('Cet évènement ne peut pas être supprimé.');
  });

  it("affiche un message générique quand l'erreur ne contient pas de message exploitable", async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(MyEventsPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/events/mine')).flush([makeEvent()]);
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelectorAll<HTMLButtonElement>('app-action-button button')[1].click();
    await stable();

    http
      .expectOne((req) => req.method === 'DELETE' && req.url.endsWith('/events/1'))
      .flush(null, { status: 500, statusText: 'Server Error' });
    await macrotask();
    await stable();

    expect(element.textContent).toContain("L'action a échoué, réessayez.");
  });
});
