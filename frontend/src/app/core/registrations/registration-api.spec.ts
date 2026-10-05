import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

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

  it("désinscrit l'utilisateur connecté de l'évènement", async () => {
    const result = service.unregister(5);

    const request = httpTesting.expectOne('/api/events/5/registrations/me');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);

    await expect(result).resolves.toBeUndefined();
  });
});
