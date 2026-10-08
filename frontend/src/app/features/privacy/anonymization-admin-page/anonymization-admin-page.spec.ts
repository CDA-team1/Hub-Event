import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Confirmation } from '../../../core/dialog/confirmation';

import { AdminAnonymizationDto } from '../../../domain/anonymization.model';
import { PageDto } from '../../../domain/page.model';
import { AnonymizationAdminPage } from './anonymization-admin-page';

function makeRequest(
  id: number,
  overrides: Partial<AdminAnonymizationDto> = {},
): AdminAnonymizationDto {
  return {
    id,
    user: {
      id: 20,
      lastName: 'Doe',
      firstName: 'Jane',
      postalAddress: '1 rue de Test',
      email: 'jane.doe@test.com',
      phone: '0600000000',
      status: 'ACTIVE',
      role: 'MEMBER',
      clubs: [
        {
          id: 10,
          name: 'Club Alpha',
        },
      ],
    },
    status: 'PENDING',
    requestDate: '2026-10-07T10:00:00',
    ...overrides,
  };
}

function pageOf(
  content: AdminAnonymizationDto[],
  overrides: Partial<PageDto<AdminAnonymizationDto>> = {},
): PageDto<AdminAnonymizationDto> {
  return {
    content,
    page: 0,
    size: 20,
    totalElements: content.length,
    totalPages: 1,
    first: true,
    last: true,
    ...overrides,
  };
}

describe('AnonymizationAdminPage', () => {
  let http: HttpTestingController;

  const stable = () => TestBed.inject(ApplicationRef).whenStable();
  const macrotask = () => new Promise((resolve) => setTimeout(resolve));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('affiche un indicateur de chargement avant la réponse', () => {
    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-loading-state')).not.toBeNull();

    http.expectOne((req) => req.url.endsWith('/admin/anonymization')).flush(pageOf([]));
  });

  it("affiche un état vide quand il n'y a aucune demande en attente", async () => {
    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();

    http.expectOne((req) => req.url.endsWith('/admin/anonymization')).flush(pageOf([]));

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-empty-state')).not.toBeNull();
    expect(element.textContent).toContain("Aucune demande d'anonymisation en attente.");
  });

  it("affiche les informations de la demande d'anonymisation", async () => {
    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();

    http
      .expectOne((req) => req.url.endsWith('/admin/anonymization'))
      .flush(pageOf([makeRequest(1)]));

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.textContent).toContain('Doe');
    expect(element.textContent).toContain('Jane');
    expect(element.textContent).toContain('jane.doe@test.com');
    expect(element.textContent).toContain('1 rue de Test');
    expect(element.textContent).toContain('Club Alpha');
    expect(element.textContent).toContain('0600000000');
  });

  it('affiche Aucun pour un utilisateur sans club et un tiret sans téléphone', async () => {
    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();

    const request = makeRequest(1, {
      user: {
        id: 20,
        lastName: 'Doe',
        firstName: 'Jane',
        postalAddress: '1 rue de Test',
        email: 'jane.doe@test.com',
        phone: null,
        status: 'ACTIVE',
        role: 'MEMBER',
        clubs: [],
      },
    });

    http.expectOne((req) => req.url.endsWith('/admin/anonymization')).flush(pageOf([request]));

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.textContent).toContain('Aucun');
    expect(element.textContent).toContain('—');
  });

  it('relance la requête avec la nouvelle page au clic sur Suivant', async () => {
    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();

    http
      .expectOne((req) => req.url.endsWith('/admin/anonymization'))
      .flush(
        pageOf([makeRequest(1)], {
          totalElements: 25,
          totalPages: 2,
        }),
      );

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    element.querySelector<HTMLButtonElement>('app-pagination button:last-child')!.click();
    TestBed.tick();

    const request = http.expectOne((req) => req.url.endsWith('/admin/anonymization'));

    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('size')).toBe('20');

    request.flush(
      pageOf(
        [
          makeRequest(2, {
            user: {
              id: 21,
              lastName: 'Martin',
              firstName: 'Paul',
              postalAddress: '2 rue de Test',
              email: 'paul.martin@test.com',
              phone: '0611111111',
              status: 'ACTIVE',
              role: 'MEMBER',
              clubs: [],
            },
          }),
        ],
        {
          page: 1,
          totalElements: 25,
          totalPages: 2,
        },
      ),
    );

    await stable();

    expect(element.textContent).toContain('Martin');
    expect(element.textContent).toContain('paul.martin@test.com');
  });

  it('affiche un état erreur si le chargement échoue', async () => {
    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();

    http
      .expectOne((req) => req.url.endsWith('/admin/anonymization'))
      .flush('Erreur serveur', {
        status: 500,
        statusText: 'Internal Server Error',
      });

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-error-state')).not.toBeNull();
    expect(element.textContent).toContain("Impossible de charger les demandes d'anonymisation.");
  });

  it('valide la demande après confirmation et recharge la liste', async () => {
    const confirm = vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();

    http
      .expectOne((req) => req.url.endsWith('/admin/anonymization'))
      .flush(pageOf([makeRequest(1)]));

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    element.querySelector<HTMLButtonElement>('button[aria-label="Valider"]')!.click();
    await stable();

    const validationRequest = http.expectOne(
      (req) => req.method === 'POST' && req.url.endsWith('/admin/anonymization/1/validate'),
    );

    validationRequest.flush({
      id: 1,
      user: {
        id: 20,
        lastName: 'ANONYMIZED',
        firstName: 'ANONYMIZED',
        postalAddress: 'ANONYMIZED',
        email: 'anonymized@test.com',
        phone: null,
        status: 'ANONYMIZED',
        role: 'MEMBER',
      },
      status: 'VALIDATED',
      requestDate: '2026-10-07T10:00:00',
    });

    await macrotask();
    TestBed.tick();

    http.expectOne((req) => req.url.endsWith('/admin/anonymization')).flush(pageOf([]));

    await stable();

    expect(confirm).toHaveBeenCalledWith(
      expect.stringContaining('irréversible'),
      'Valider',
      'Annuler',
    );

    expect(element.querySelector('app-empty-state')).not.toBeNull();
  });

  it("ne valide pas la demande si l'administrateur annule la confirmation", async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(false);

    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();

    http
      .expectOne((req) => req.url.endsWith('/admin/anonymization'))
      .flush(pageOf([makeRequest(1)]));

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    element.querySelector<HTMLButtonElement>('button[aria-label="Valider"]')!.click();

    await stable();

    expect(element.textContent).toContain('Doe');
    expect(element.querySelector('button[aria-label="Valider"]')).not.toBeNull();
  });

  it('affiche une erreur si la validation échoue', async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();

    http
      .expectOne((req) => req.url.endsWith('/admin/anonymization'))
      .flush(pageOf([makeRequest(1)]));

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    element.querySelector<HTMLButtonElement>('button[aria-label="Valider"]')!.click();
    await stable();

    http
      .expectOne(
        (req) => req.method === 'POST' && req.url.endsWith('/admin/anonymization/1/validate'),
      )
      .flush('Erreur serveur', {
        status: 500,
        statusText: 'Internal Server Error',
      });

    await macrotask();
    await stable();

    expect(element.querySelector('[role="alert"]')?.textContent).toContain(
      "La validation de la demande d'anonymisation a échoué. Réessayez.",
    );
  });

  it('affiche le bouton Valider avec le niveau success', async () => {
    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();

    http
      .expectOne((req) => req.url.endsWith('/admin/anonymization'))
      .flush(pageOf([makeRequest(1)]));

    await stable();

    const button = (fixture.nativeElement as HTMLElement).querySelector(
      'button[aria-label="Valider"]',
    );

    expect(button).not.toBeNull();

    const actionButton = (fixture.nativeElement as HTMLElement).querySelector('app-action-button');

    expect(actionButton).not.toBeNull();
  });

  it('reprend les coordonnées et les clubs sous le nom', async () => {
    const fixture = TestBed.createComponent(AnonymizationAdminPage);
    TestBed.tick();
    http
      .expectOne((req) => req.url.endsWith('/admin/anonymization'))
      .flush(pageOf([makeRequest(1)]));
    await stable();

    const meta = (fixture.nativeElement as HTMLElement).querySelector('tbody td .request-meta')!;

    expect(meta.textContent).toContain('Jane');
    expect(meta.textContent).toContain('jane.doe@test.com');
    expect(meta.textContent).toContain('1 rue de Test');
    expect(meta.textContent).toContain('Clubs : Club Alpha');
    expect(meta.textContent).toContain('0600000000');
  });
});
