import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { CommentDto } from '../../domain/event.model';
import { API_URL } from '../http/api-url';
import { CommentApi } from './comment-api';

describe('CommentApi', () => {
  let service: CommentApi;
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

    service = TestBed.inject(CommentApi);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('publie un commentaire sur un évènement', async () => {
    const created: CommentDto = {
      id: 12,
      eventId: 5,
      authorDisplayName: 'Jean D.',
      content: 'Super évènement !',
      createdAt: '2026-10-06T09:30:00',
    };

    const result = service.create(5, 'Super évènement !');

    const request = httpTesting.expectOne('/api/comments');

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      eventId: 5,
      content: 'Super évènement !',
    });

    request.flush(created);

    await expect(result).resolves.toEqual(created);
  });

  it('propage le message du back quand le commentaire est refusé', async () => {
    const result = service.create(5, '');

    httpTesting.expectOne('/api/comments').flush('Veuillez saisir un commentaire.', {
      status: 400,
      statusText: 'Bad Request',
    });

    await expect(result).rejects.toMatchObject({
      status: 400,
      error: 'Veuillez saisir un commentaire.',
    });
  });
});
