import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { EventDetailResponse } from '../../../domain/event.model';
import { EventCancelPage } from './event-cancel-page';

function makeDetail(): EventDetailResponse {
  return {
    title: 'Tournoi de printemps',
    description: 'Description',
    location: 'Lyon',
    startDateTime: '2026-11-01T18:00:00',
    endDateTime: null,
    affiliatedPrice: 5,
    nonAffiliatedPrice: 8,
    maxSeats: 50,
    category: 'SPORT',
    status: 'PUBLISHED',
    remainingSeats: 47,
    waitingCount: 0,
    owner: true,
    myRegistration: null,
    gallery: [],
  };
}

describe('EventCancelPage', () => {
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

  async function open(id = '5') {
    configure(id);
    const fixture = TestBed.createComponent(EventCancelPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith(`/events/${id}`)).flush(makeDetail());
    await stable();
    return fixture.nativeElement as HTMLElement;
  }

  afterEach(() => http.verify());

  it('affiche un indicateur de chargement avant la réponse', () => {
    configure('5');
    const fixture = TestBed.createComponent(EventCancelPage);
    TestBed.tick();

    expect((fixture.nativeElement as HTMLElement).querySelector('app-loading-state')).not.toBeNull();

    http.expectOne((req) => req.url.endsWith('/events/5')).flush(makeDetail());
  });

  it("affiche le titre de l'évènement, les boutons Valider / Annuler et l'effet de l'annulation", async () => {
    const element = await open();

    expect(element.textContent).toContain('Tournoi de printemps');
    expect(element.querySelector('.confirm')?.textContent?.trim()).toBe('Valider');
    expect(element.querySelector('.cancel')?.textContent?.trim()).toBe('Annuler');
    expect(element.textContent).toContain("liste d'attente");
  });

  it('annule l’évènement au clic sur Valider puis redirige vers la liste', async () => {
    const element = await open();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    element.querySelector<HTMLButtonElement>('.confirm')!.click();
    await stable();

    http.expectOne((req) => req.method === 'POST' && req.url.endsWith('/events/5/cancel')).flush({});
    await stable();

    expect(navigate).toHaveBeenCalledWith(['/mes-evenements']);
  });

  it('répercute le message du back si l’annulation est refusée, sans rediriger', async () => {
    const element = await open();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    element.querySelector<HTMLButtonElement>('.confirm')!.click();
    await stable();

    http
      .expectOne((req) => req.method === 'POST' && req.url.endsWith('/events/5/cancel'))
      .flush('Cet événement ne peut pas être annulé dans son état actuel.', {
        status: 400,
        statusText: 'Bad Request',
      });
    await stable();

    expect(element.textContent).toContain('Cet événement ne peut pas être annulé dans son état actuel.');
    expect(navigate).not.toHaveBeenCalled();
  });

  it('affiche un message générique quand l’erreur ne contient pas de message exploitable', async () => {
    const element = await open();

    element.querySelector<HTMLButtonElement>('.confirm')!.click();
    await stable();

    http
      .expectOne((req) => req.method === 'POST' && req.url.endsWith('/events/5/cancel'))
      .flush(null, { status: 500, statusText: 'Server Error' });
    await stable();

    expect(element.textContent).toContain("L'annulation a échoué, réessayez.");
  });

  it('revient à la liste sans appel réseau au clic sur Annuler', async () => {
    const element = await open();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    element.querySelector<HTMLButtonElement>('.cancel')!.click();
    await stable();

    expect(navigate).toHaveBeenCalledWith(['/mes-evenements']);
  });

  it("affiche une erreur sans appeler le serveur quand l'identifiant est invalide", async () => {
    configure('abc');
    const fixture = TestBed.createComponent(EventCancelPage);
    TestBed.tick();
    await stable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Évènement introuvable.');
  });
});
