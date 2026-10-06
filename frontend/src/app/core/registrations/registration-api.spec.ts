import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { RegistrationDto } from '../../domain/event.model';
import { API_URL } from '../http/api-url';
import { RegistrationApi } from './registration-api';

describe('RegistrationApi', () => {
  let service: RegistrationApi;
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

    service = TestBed.inject(RegistrationApi);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it("inscrit l'utilisateur connecté à l'évènement", async () => {
    const created: RegistrationDto = {
      id: 1,
      eventId: 5,
      userId: 20,
      userEmail: 'jean@example.com',
      status: 'REGISTERED',
      registrationDate: '2026-10-05T10:00:00',
    };

    const result = service.register(5);

    const request = httpTesting.expectOne('/api/events/5/registrations');
    expect(request.request.method).toBe('POST');
    request.flush(created);

    await expect(result).resolves.toEqual(created);
  });

  it("propage le message du back quand l'inscription est refusée", async () => {
    const result = service.register(5);

    httpTesting
      .expectOne('/api/events/5/registrations')
      .flush('Vous êtes déjà inscrit à cet évènement.', {
        status: 400,
        statusText: 'Bad Request',
      });

    await expect(result).rejects.toMatchObject({
      status: 400,
      error: 'Vous êtes déjà inscrit à cet évènement.',
    });
  });

  it("désinscrit l'utilisateur connecté de l'évènement", async () => {
    const result = service.unregister(5);

    const request = httpTesting.expectOne('/api/events/5/registrations/me');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);

    await expect(result).resolves.toBeUndefined();
  });

  it("désinscrit un membre à la demande de l'organisateur, avec le motif", async () => {
    const result = service.cancelByOrganizer(5, 20, 'Comportement inapproprié');

    const request = httpTesting.expectOne('/api/events/5/registrations/20');
    expect(request.request.method).toBe('DELETE');
    expect(request.request.body).toEqual({ reason: 'Comportement inapproprié' });
    request.flush(null);

    await expect(result).resolves.toBeUndefined();
  });
});
