import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { EventCardDto } from '../../../domain/event.model';
import { EventSearchPage } from './event-search-page';

const RESULTS: EventCardDto[] = [
  {
    id: 1,
    title: 'Yoga au lever du soleil',
    location: 'Parc Borély, 13008 Marseille',
    startDateTime: '2026-10-11T07:45:00',
    endDateTime: null,
    affiliatedPrice: 0,
    nonAffiliatedPrice: 6,
    category: 'SPORT',
    imageUrl: null,
  },
  {
    id: 2,
    title: 'Tournoi de futsal nocturne',
    location: 'Complexe sportif, 59000 Lille',
    startDateTime: '2026-10-23T20:00:00',
    endDateTime: null,
    affiliatedPrice: 5,
    nonAffiliatedPrice: 10,
    category: 'SPORT',
    imageUrl: null,
  },
];

describe('EventSearchPage', () => {
  let http: HttpTestingController;

  const stable = () => TestBed.inject(ApplicationRef).whenStable();
  const settle = () => new Promise<void>((resolve) => setTimeout(resolve));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'recherche', component: EventSearchPage }]),
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  async function open(url: string): Promise<HTMLElement> {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url, EventSearchPage);
    TestBed.tick();
    return harness.routeNativeElement as HTMLElement;
  }

  const searchRequests = () => http.match((req) => req.url === '/api/events/search');

  it('affiche un indicateur de chargement avant la réponse', async () => {
    const element = await open('/recherche');

    expect(element.querySelector('app-loading-state')).not.toBeNull();

    http.expectOne('/api/events/search').flush([]);
  });

  it('affiche les cartes des événements trouvés', async () => {
    const element = await open('/recherche');
    http.expectOne('/api/events/search').flush(RESULTS);
    await stable();

    expect(element.querySelectorAll('app-event-card')).toHaveLength(2);
    expect(element.textContent).toContain('Yoga au lever du soleil');
    expect(element.textContent).toContain('Tournoi de futsal nocturne');
  });

  it('affiche un message quand aucun événement ne correspond', async () => {
    const element = await open('/recherche');
    http.expectOne('/api/events/search').flush([]);
    await stable();

    expect(element.querySelector('app-empty-state')?.textContent).toContain(
      'Aucun événement ne correspond',
    );
    expect(element.querySelector('app-event-card')).toBeNull();
  });

  it("envoie au serveur les critères lus dans l'URL", async () => {
    await open('/recherche?category=SPORT&maxPrice=10&keywords=yoga');

    const [request] = searchRequests();
    expect(request.request.params.get('category')).toBe('SPORT');
    expect(request.request.params.get('maxPrice')).toBe('10');
    expect(request.request.params.get('keywords')).toBe('yoga');
    request.flush([]);
  });

  it("pré-remplit le formulaire avec les critères de l'URL", async () => {
    const element = await open('/recherche?category=SPORT&keywords=yoga');
    searchRequests()[0].flush([]);
    await stable();

    expect(element.querySelector<HTMLSelectElement>('#search-category')?.value).toBe('SPORT');
    expect(element.querySelector<HTMLInputElement>('#search-keywords')?.value).toBe('yoga');
  });

  it('affiche une erreur avec un bouton Réessayer qui relance la recherche', async () => {
    const element = await open('/recherche');
    http.expectOne('/api/events/search').flush('boom', { status: 500, statusText: 'Server Error' });
    await stable();

    expect(element.querySelector('app-error-state')).not.toBeNull();

    element.querySelector<HTMLButtonElement>('app-error-state button')!.click();
    TestBed.tick();
    http.expectOne('/api/events/search').flush(RESULTS);
    await stable();

    expect(element.querySelector('app-error-state')).toBeNull();
    expect(element.querySelectorAll('app-event-card')).toHaveLength(2);
  });

  it("met l'URL à jour et relance la recherche quand on valide le formulaire", async () => {
    const element = await open('/recherche');
    http.expectOne('/api/events/search').flush([]);
    await stable();

    const keywords = element.querySelector<HTMLInputElement>('#search-keywords')!;
    keywords.value = 'yoga';
    keywords.dispatchEvent(new Event('input'));
    keywords.dispatchEvent(new Event('change'));
    keywords.dispatchEvent(new Event('blur'));
    await settle();

    element.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
    await settle();
    TestBed.tick();

    expect(TestBed.inject(Router).url).toBe('/recherche?keywords=yoga');
    const [request] = searchRequests();
    expect(request.request.params.get('keywords')).toBe('yoga');
    request.flush(RESULTS);
  });
});
