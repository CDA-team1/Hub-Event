import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

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

  const stable = () => TestBed.inject(ApplicationRef).whenStable();

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'evenements/:id', component: EventDetailPage }]),
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
});
