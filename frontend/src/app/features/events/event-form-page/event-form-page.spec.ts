import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { EventDetailResponse } from '../../../domain/event.model';
import { EventFormPage } from './event-form-page';

function makeDetail(overrides: Partial<EventDetailResponse> = {}): EventDetailResponse {
  return {
    title: 'Évènement existant',
    description: 'Description existante',
    location: 'Lyon',
    startDateTime: '2026-11-01T18:00:00',
    endDateTime: null,
    affiliatedPrice: 5,
    nonAffiliatedPrice: 8,
    maxSeats: 50,
    category: 'SPORT',
    status: 'DRAFT',
    remainingSeats: 50,
    waitingCount: 0,
    owner: true,
    myRegistration: null,
    gallery: [],
    ...overrides,
  };
}

function provideRoute(id?: string) {
  return {
    provide: ActivatedRoute,
    useValue: { snapshot: { paramMap: convertToParamMap(id ? { id } : {}) } },
  };
}

describe('EventFormPage', () => {
  let http: HttpTestingController;

  const stable = async () => {
    await new Promise((resolve) => setTimeout(resolve));
    await TestBed.inject(ApplicationRef).whenStable();
  };

  async function type(element: HTMLElement, selector: string, value: string): Promise<void> {
    const field = element.querySelector<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>(
      selector,
    )!;
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('change'));
    field.dispatchEvent(new Event('blur'));
    await stable();
  }

  function submitForm(element: HTMLElement): void {
    element.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
  }

  async function fillRequiredFields(element: HTMLElement): Promise<void> {
    await type(element, '#title', 'Concert');
    await type(element, '#category', 'CULTURE');
    await type(element, '#description', 'Un bel évènement');
    await type(element, '#location', 'Paris');
    await type(element, '#maxSeats', '100');
    await type(element, '#startDateTime', '2026-12-01T20:00');
    await type(element, '#affiliatedPrice', '10');
    await type(element, '#nonAffiliatedPrice', '15');
  }

  afterEach(() => http.verify());

  describe('en création', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          provideHttpClient(),
          provideHttpClientTesting(),
          provideRouter([]),
          provideRoute(),
        ],
      });
      http = TestBed.inject(HttpTestingController);
    });

    it('affiche le formulaire vide une fois les clubs chargés', async () => {
      const fixture = TestBed.createComponent(EventFormPage);
      TestBed.tick();
      http.expectOne((req) => req.url.endsWith('/clubs/mine')).flush([
        { id: 1, name: 'Club A', category: 'SPORT', postalAddress: '', email: '', phone: '', validityEndDate: null, members: [] },
      ]);
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      expect(element.textContent).toContain('Créer un évènement');
      expect(element.querySelector('form')).not.toBeNull();
      expect(element.querySelector('#clubId')).not.toBeNull();
    });

    it("affiche un message et aucun formulaire si l'organisateur n'a aucun club", async () => {
      const fixture = TestBed.createComponent(EventFormPage);
      TestBed.tick();
      http.expectOne((req) => req.url.endsWith('/clubs/mine')).flush([]);
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      expect(element.textContent).toContain('Vous devez être affilié à un club');
      expect(element.querySelector('form')).toBeNull();
    });

    it('crée un évènement puis redirige vers la liste', async () => {
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const fixture = TestBed.createComponent(EventFormPage);
      TestBed.tick();
      http.expectOne((req) => req.url.endsWith('/clubs/mine')).flush([
        { id: 1, name: 'Club A', category: 'SPORT', postalAddress: '', email: '', phone: '', validityEndDate: null, members: [] },
      ]);
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      await fillRequiredFields(element);
      await type(element, '#clubId', '1');
      submitForm(element);
      await stable();

      const request = http.expectOne((req) => req.method === 'POST' && req.url.endsWith('/events'));
      expect(request.request.body).toEqual({
        title: 'Concert',
        description: 'Un bel évènement',
        location: 'Paris',
        startDateTime: '2026-12-01T20:00',
        endDateTime: null,
        affiliatedPrice: 10,
        nonAffiliatedPrice: 15,
        maxSeats: 100,
        category: 'CULTURE',
        clubId: 1,
      });
      request.flush({});
      await stable();

      expect(navigate).toHaveBeenCalledWith(['/mes-evenements']);
    });

    it("affiche le message du back et ne redirige pas si la création échoue", async () => {
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const fixture = TestBed.createComponent(EventFormPage);
      TestBed.tick();
      http.expectOne((req) => req.url.endsWith('/clubs/mine')).flush([
        { id: 1, name: 'Club A', category: 'SPORT', postalAddress: '', email: '', phone: '', validityEndDate: null, members: [] },
      ]);
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      await fillRequiredFields(element);
      await type(element, '#clubId', '1');
      submitForm(element);
      await stable();

      http
        .expectOne((req) => req.method === 'POST' && req.url.endsWith('/events'))
        .flush('Veuillez renseigner tous les champs obligatoires.', {
          status: 400,
          statusText: 'Bad Request',
        });
      await stable();

      expect(element.textContent).toContain('Veuillez renseigner tous les champs obligatoires.');
      expect(navigate).not.toHaveBeenCalled();
    });
  });

  describe('en modification', () => {
    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          provideHttpClient(),
          provideHttpClientTesting(),
          provideRouter([]),
          provideRoute('1'),
        ],
      });
      http = TestBed.inject(HttpTestingController);
    });

    it("charge l'évènement et pré-remplit le formulaire, sans champ Club", async () => {
      const fixture = TestBed.createComponent(EventFormPage);
      TestBed.tick();
      http.expectOne((req) => req.url.endsWith('/events/1')).flush(makeDetail());
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      expect(element.textContent).toContain("Modifier l'évènement");
      expect(element.querySelector<HTMLInputElement>('#title')?.value).toBe('Évènement existant');
      expect(element.querySelector('#clubId')).toBeNull();
    });

    it('modifie un évènement puis redirige vers la liste', async () => {
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const fixture = TestBed.createComponent(EventFormPage);
      TestBed.tick();
      http.expectOne((req) => req.url.endsWith('/events/1')).flush(makeDetail());
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      await type(element, '#title', 'Titre modifié');
      submitForm(element);
      await stable();

      const request = http.expectOne((req) => req.method === 'PUT' && req.url.endsWith('/events/1'));
      expect(request.request.body.title).toBe('Titre modifié');
      expect(request.request.body.clubId).toBeUndefined();
      request.flush({});
      await stable();

      expect(navigate).toHaveBeenCalledWith(['/mes-evenements']);
    });

    it('verrouille le formulaire quand l’évènement est terminé', async () => {
      const fixture = TestBed.createComponent(EventFormPage);
      TestBed.tick();
      http.expectOne((req) => req.url.endsWith('/events/1')).flush(makeDetail({ status: 'FINISHED' }));
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      expect(element.querySelector<HTMLInputElement>('#title')?.disabled).toBe(true);
    });

    it("émet l'annulation sans appel réseau et redirige vers la liste", async () => {
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const fixture = TestBed.createComponent(EventFormPage);
      TestBed.tick();
      http.expectOne((req) => req.url.endsWith('/events/1')).flush(makeDetail());
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      element.querySelector<HTMLButtonElement>('button[type="button"]')!.click();
      await stable();

      expect(navigate).toHaveBeenCalledWith(['/mes-evenements']);
    });
  });
});
