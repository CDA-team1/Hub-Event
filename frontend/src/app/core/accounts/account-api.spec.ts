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
  it('récupère le détail d’un compte avec ses clubs', () => {
    service.getUserById(12).subscribe((user) => {
      expect(user.id).toBe(12);
      expect(user.clubs).toHaveLength(1);
      expect(user.clubs[0].id).toBe(3);
    });

    const request = httpTesting.expectOne('/api/admin/users/12');

    expect(request.request.method).toBe('GET');

    request.flush({
      id: 12,
      lastName: 'Doe',
      firstName: 'John',
      postalAddress: '1 rue de Test',
      email: 'john.doe@test.com',
      phone: null,
      status: 'ACTIVE',
      role: 'MEMBER',
      clubs: [{ id: 3, name: 'Club de Test' }],
    });
  });

  it('crée un compte utilisateur', () => {
    const body = {
      lastName: 'Doe',
      firstName: 'Jane',
      postalAddress: '1 rue de Test',
      email: 'jane.doe@test.com',
      phone: '0600000000',
      role: 'MEMBER' as const,
      clubIds: [3],
    };

    service.createUser(body).subscribe();

    const request = httpTesting.expectOne('/api/admin/users');

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);

    request.flush({
      id: 12,
      ...body,
      status: 'INACTIVE',
    });
  });

  it('modifie un compte utilisateur', () => {
    const body = {
      lastName: 'Doe',
      firstName: 'Jane',
      postalAddress: '2 rue de Test',
      email: 'jane.doe@test.com',
      phone: '0611111111',
      role: 'ORGANIZER' as const,
      clubIds: [3, 4],
    };

    service.updateUser(12, body).subscribe();

    const request = httpTesting.expectOne('/api/admin/users/12');

    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(body);

    request.flush({
      id: 12,
      lastName: body.lastName,
      firstName: body.firstName,
      postalAddress: body.postalAddress,
      email: body.email,
      phone: body.phone,
      role: body.role,
      status: 'ACTIVE',
    });
  });
});
