import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { EventsHomePage } from './events-home-page';

const EMPTY_LIST = { cultureEvents: [], leisureEvents: [], sportEvents: [], pastEvents: [] };

describe('EventsHomePage', () => {
  let http: HttpTestingController;

  const stable = () => TestBed.inject(ApplicationRef).whenStable();

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('affiche un indicateur de chargement avant la réponse', () => {
    const fixture = TestBed.createComponent(EventsHomePage);
    TestBed.tick();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-loading-state')).not.toBeNull();

    http.expectOne('/api/events').flush(EMPTY_LIST);
  });

  it('affiche un état vide pour chaque section sans évènement', async () => {
    const fixture = TestBed.createComponent(EventsHomePage);
    TestBed.tick();
    http.expectOne('/api/events').flush(EMPTY_LIST);
    await stable();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelectorAll('app-empty-state')).toHaveLength(4);
  });

  it('affiche une erreur avec un bouton Réessayer qui relance la requête', async () => {
    const fixture = TestBed.createComponent(EventsHomePage);
    TestBed.tick();
    http.expectOne('/api/events').flush('boom', { status: 500, statusText: 'Server Error' });
    await stable();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-error-state')).not.toBeNull();

    element.querySelector<HTMLButtonElement>('app-error-state button')!.click();
    TestBed.tick();
    http.expectOne('/api/events').flush(EMPTY_LIST);
    await stable();

    expect(element.querySelector('app-error-state')).toBeNull();
  });
});
