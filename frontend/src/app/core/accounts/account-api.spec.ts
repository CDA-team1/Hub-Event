import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { API_URL } from '../http/api-url';
import { AccountApi } from './account-api';

describe('AccountApi', () => {
  let service: AccountApi;
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

    service = TestBed.inject(AccountApi);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('récupère une page de comptes sans filtre de rôle', () => {
    service.getUsers(null, 2).subscribe();

    const request = httpTesting.expectOne(
      (req) =>
        req.url === '/api/admin/users' && req.params.get('page') === '2' && !req.params.has('role'),
    );

    expect(request.request.method).toBe('GET');

    request.flush({
      content: [],
      page: 2,
      size: 20,
      totalElements: 0,
      totalPages: 0,
      first: false,
      last: true,
    });
  });

  it('ajoute le rôle aux paramètres de recherche', () => {
    service.getUsers('ADMIN', 0).subscribe();

    const request = httpTesting.expectOne(
      (req) =>
        req.url === '/api/admin/users' &&
        req.params.get('page') === '0' &&
        req.params.get('role') === 'ADMIN',
    );

    expect(request.request.method).toBe('GET');

    request.flush({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
      first: true,
      last: true,
    });
  });
});
