import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApplicationRef, signal } from '@angular/core';

import { AnonymizationDto } from '../../domain/anonymization.model';
import { API_URL } from '../http/api-url';
import { AnonymizationApi } from './anonymization-api';

describe('AnonymizationApi', () => {
  let service: AnonymizationApi;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: API_URL,
          useValue: '/api',
        },
      ],
    });

    service = TestBed.inject(AnonymizationApi);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it("crée une demande d'anonymisation pour l'utilisateur connecté", async () => {
    const created: AnonymizationDto = {
      id: 1,
      user: {
        id: 20,
        lastName: 'Doe',
        firstName: 'Jane',
        postalAddress: '1 rue de Test',
        email: 'jane.doe@test.com',
        phone: '0600000000',
        status: 'ACTIVE',
        role: 'MEMBER',
      },
      status: 'PENDING',
      requestDate: '2026-10-06T16:00:00',
    };

    const result = service.createRequest();

    const request = httpTesting.expectOne('/api/anonymization-requests');

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeNull();

    request.flush(created, {
      status: 201,
      statusText: 'Created',
    });

    await expect(result).resolves.toEqual(created);
  });

  it('propage l’erreur du back si la demande ne peut pas être créée', async () => {
    const result = service.createRequest();

    httpTesting
      .expectOne('/api/anonymization-requests')
      .flush('Une demande d’anonymisation existe déjà.', {
        status: 400,
        statusText: 'Bad Request',
      });

    await expect(result).rejects.toMatchObject({
      status: 400,
      error: 'Une demande d’anonymisation existe déjà.',
    });
  });

  it("récupère une page de demandes d'anonymisation en attente", async () => {
    const params = signal({
      page: 0,
      size: 20,
    });

    const resource = TestBed.runInInjectionContext(() => service.listPending(params));

    TestBed.tick();

    const request = httpTesting.expectOne(
      (req) =>
        req.url === '/api/admin/anonymization' &&
        req.params.get('page') === '0' &&
        req.params.get('size') === '20',
    );

    expect(request.request.method).toBe('GET');

    request.flush({
      content: [
        {
          id: 1,
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
                name: 'Club Test',
              },
            ],
          },
          status: 'PENDING',
          requestDate: '2026-10-07T10:00:00',
        },
      ],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
    });

    await TestBed.inject(ApplicationRef).whenStable();

    expect(resource.value().content).toHaveLength(1);
    expect(resource.value().content[0].user.email).toBe('jane.doe@test.com');
    expect(resource.value().content[0].user.clubs).toHaveLength(1);
    expect(resource.value().content[0].user.clubs[0].name).toBe('Club Test');
    expect(resource.value().totalElements).toBe(1);
  });
});
