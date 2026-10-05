import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { API_URL } from '../http/api-url';
import { ClubApi } from './club-api';

describe('ClubApi', () => {
  let service: ClubApi;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ClubApi,
        {
          provide: API_URL,
          useValue: '/api',
        },
      ],
    });

    service = TestBed.inject(ClubApi);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('récupère une page de clubs', () => {
    service.getClubs(0, 20).subscribe((page) => {
      expect(page.content).toHaveLength(2);
      expect(page.content[0].id).toBe(1);
      expect(page.content[0].name).toBe('Club A');
      expect(page.content[1].id).toBe(2);
      expect(page.totalElements).toBe(2);
    });

    const request = httpTesting.expectOne(
      (req) =>
        req.url === '/api/clubs' &&
        req.params.get('page') === '0' &&
        req.params.get('size') === '20',
    );

    expect(request.request.method).toBe('GET');

    request.flush({
      content: [
        {
          id: 1,
          name: 'Club A',
          category: 'SPORT',
          postalAddress: '1 rue du Sport',
          email: 'club-a@test.com',
          phone: '0600000001',
          validityEndDate: null,
        },
        {
          id: 2,
          name: 'Club B',
          category: 'CULTURE',
          postalAddress: '2 rue de la Culture',
          email: 'club-b@test.com',
          phone: '0600000002',
          validityEndDate: null,
        },
      ],
      page: 0,
      size: 20,
      totalElements: 2,
      totalPages: 1,
      first: true,
      last: true,
    });
  });

  it('supprime un club', async () => {
    const promise = service.delete(1);

    const request = httpTesting.expectOne('/api/clubs/1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);

    await expect(promise).resolves.toBeUndefined();
  });
});
