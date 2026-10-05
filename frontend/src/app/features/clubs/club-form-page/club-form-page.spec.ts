import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { ClubDto } from '../../../domain/club.model';
import { ClubFormPage } from './club-form-page';

function makeClub(id: number): ClubDto {
  return {
    id,
    name: 'Club existant',
    category: 'CULTURE',
    postalAddress: '2 rue de Lyon',
    email: 'existant@test.fr',
    phone: '0700000000',
    validityEndDate: null,
    members: [],
  };
}

function provideRoute(id?: string) {
  return {
    provide: ActivatedRoute,
    useValue: { snapshot: { paramMap: convertToParamMap(id ? { id } : {}) } },
  };
}

describe('ClubFormPage', () => {
  let http: HttpTestingController;

  const stable = async () => {
    await new Promise((resolve) => setTimeout(resolve));
    await TestBed.inject(ApplicationRef).whenStable();
  };

  async function type(element: HTMLElement, selector: string, value: string): Promise<void> {
    const field = element.querySelector<HTMLInputElement | HTMLSelectElement>(selector)!;
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('change'));
    field.dispatchEvent(new Event('blur'));
    await stable();
  }

  function submitForm(element: HTMLElement): void {
    element.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
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

    it('affiche le formulaire vide sans requête de chargement', async () => {
      const fixture = TestBed.createComponent(ClubFormPage);
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      expect(element.textContent).toContain('Ajouter un club');
      expect(element.querySelector('form')).not.toBeNull();
    });

    it('crée le club puis redirige vers la liste', async () => {
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const fixture = TestBed.createComponent(ClubFormPage);
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      await type(element, '#name', 'Club de boxe');
      await type(element, '#category', 'SPORT');
      await type(element, '#postalAddress', '1 rue de Paris');
      await type(element, '#email', 'club@test.fr');
      await type(element, '#phone', '0600000000');
      submitForm(element);
      await stable();

      const request = http.expectOne((req) => req.method === 'POST' && req.url.endsWith('/clubs'));
      expect(request.request.body).toEqual({
        name: 'Club de boxe',
        category: 'SPORT',
        postalAddress: '1 rue de Paris',
        email: 'club@test.fr',
        phone: '0600000000',
      });
      request.flush(makeClub(1));
      await stable();

      expect(navigate).toHaveBeenCalledWith(['/clubs']);
    });

    it("affiche le message du back et ne redirige pas si la création échoue", async () => {
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const fixture = TestBed.createComponent(ClubFormPage);
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      await type(element, '#name', 'Club de boxe');
      await type(element, '#category', 'SPORT');
      await type(element, '#postalAddress', '1 rue de Paris');
      await type(element, '#email', 'club@test.fr');
      await type(element, '#phone', '0600000000');
      submitForm(element);
      await stable();

      http
        .expectOne((req) => req.method === 'POST' && req.url.endsWith('/clubs'))
        .flush('Le nom du club est obligatoire', { status: 400, statusText: 'Bad Request' });
      await stable();

      expect(element.textContent).toContain('Le nom du club est obligatoire');
      expect(navigate).not.toHaveBeenCalled();
    });

    it("émet l'annulation sans appel réseau et redirige vers la liste", async () => {
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const fixture = TestBed.createComponent(ClubFormPage);
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      element.querySelector<HTMLButtonElement>('button[type="button"]')!.click();
      await stable();

      expect(navigate).toHaveBeenCalledWith(['/clubs']);
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

    it('charge le club et pré-remplit le formulaire', async () => {
      const fixture = TestBed.createComponent(ClubFormPage);
      TestBed.tick();
      const element = fixture.nativeElement as HTMLElement;

      expect(element.querySelector('app-loading-state')).not.toBeNull();

      http.expectOne((req) => req.url.endsWith('/clubs/1')).flush(makeClub(1));
      await stable();

      expect(element.textContent).toContain('Modifier le club');
      expect(element.querySelector<HTMLInputElement>('#name')?.value).toBe('Club existant');
    });

    it('modifie le club puis redirige vers la liste', async () => {
      const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
      const fixture = TestBed.createComponent(ClubFormPage);
      TestBed.tick();
      http.expectOne((req) => req.url.endsWith('/clubs/1')).flush(makeClub(1));
      await stable();
      const element = fixture.nativeElement as HTMLElement;

      await type(element, '#name', 'Club renommé');
      submitForm(element);
      await stable();

      const request = http.expectOne((req) => req.method === 'PUT' && req.url.endsWith('/clubs/1'));
      expect(request.request.body.name).toBe('Club renommé');
      request.flush(makeClub(1));
      await stable();

      expect(navigate).toHaveBeenCalledWith(['/clubs']);
    });
  });
});
