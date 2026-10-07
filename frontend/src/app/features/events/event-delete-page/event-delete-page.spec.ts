import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { EventDetailResponse } from '../../../domain/event.model';
import { EventDeletePage } from './event-delete-page';

function makeDetail(): EventDetailResponse {
  return {
    title: 'Concert de printemps',
    description: 'Description',
    location: 'Lyon',
    startDateTime: '2026-11-01T18:00:00',
    endDateTime: null,
    affiliatedPrice: 5,
    nonAffiliatedPrice: 8,
    maxSeats: 50,
    category: 'CULTURE',
    status: 'DRAFT',
    remainingSeats: 50,
    waitingCount: 0,
    owner: true,
    myRegistration: null,
    gallery: [],
  };
}

describe('EventDeletePage', () => {
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
    const fixture = TestBed.createComponent(EventDeletePage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith(`/events/${id}`)).flush(makeDetail());
    await stable();
    return fixture.nativeElement as HTMLElement;
  }

  afterEach(() => http.verify());

  it('affiche un indicateur de chargement avant la réponse', () => {
    configure('5');
    const fixture = TestBed.createComponent(EventDeletePage);
    TestBed.tick();

    expect((fixture.nativeElement as HTMLElement).querySelector('app-loading-state')).not.toBeNull();

    http.expectOne((req) => req.url.endsWith('/events/5')).flush(makeDetail());
  });

  it("affiche le titre de l'évènement et les boutons Valider / Annuler", async () => {
    const element = await open();

    expect(element.textContent).toContain('Concert de printemps');
    expect(element.querySelector('.confirm')?.textContent?.trim()).toBe('Valider');
    expect(element.querySelector('.cancel')?.textContent?.trim()).toBe('Annuler');
  });

  it('supprime l’évènement au clic sur Valider puis redirige vers la liste', async () => {
    const element = await open();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    element.querySelector<HTMLButtonElement>('.confirm')!.click();
    await stable();

    http.expectOne((req) => req.method === 'DELETE' && req.url.endsWith('/events/5')).flush(null);
    await stable();

    expect(navigate).toHaveBeenCalledWith(['/mes-evenements']);
  });

  it('répercute le message du back (« doit être annulé ») et ne redirige pas', async () => {
    const element = await open();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    element.querySelector<HTMLButtonElement>('.confirm')!.click();
    await stable();

    http
      .expectOne((req) => req.method === 'DELETE' && req.url.endsWith('/events/5'))
      .flush('Cet événement ne peut pas être supprimé car il possède des inscrits. Il doit être annulé.', {
        status: 400,
        statusText: 'Bad Request',
      });
    await stable();

    expect(element.textContent).toContain('Il doit être annulé.');
    expect(navigate).not.toHaveBeenCalled();
  });

  it('affiche un message générique quand l’erreur ne contient pas de message exploitable', async () => {
    const element = await open();

    element.querySelector<HTMLButtonElement>('.confirm')!.click();
    await stable();

    http
      .expectOne((req) => req.method === 'DELETE' && req.url.endsWith('/events/5'))
      .flush(null, { status: 500, statusText: 'Server Error' });
    await stable();

    expect(element.textContent).toContain('La suppression a échoué, réessayez.');
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
    const fixture = TestBed.createComponent(EventDeletePage);
    TestBed.tick();
    await stable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Évènement introuvable.');
  });
});
