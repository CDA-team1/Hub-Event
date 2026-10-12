import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { LegalDocumentDto } from '../../domain/legal-document.model';
import { API_URL } from '../http/api-url';
import { LegalDocumentApi } from './legal-document-api';

describe('LegalDocumentApi', () => {
  let service: LegalDocumentApi;
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

    service = TestBed.inject(LegalDocumentApi);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('récupère les CGU', () => {
    const response: LegalDocumentDto = {
      id: 1,
      type: 'CGU',
      content: 'Conditions générales d’utilisation',
      updatedAt: '2026-10-05T12:00:00',
    };

    service.getByType('CGU').subscribe((document) => {
      expect(document).toEqual(response);
    });

    const request = httpTesting.expectOne('/api/documents/CGU');

    expect(request.request.method).toBe('GET');

    request.flush(response);
  });

  it('retourne null si aucun document légal n’est disponible', () => {
    service.getByType('RGPD').subscribe((document) => {
      expect(document).toBeNull();
    });

    const request = httpTesting.expectOne('/api/documents/RGPD');

    expect(request.request.method).toBe('GET');

    request.flush(null, {
      status: 204,
      statusText: 'No Content',
    });
  });

  it('enregistre un document légal', () => {
    const response: LegalDocumentDto = {
      id: 2,
      type: 'RGPD',
      content: 'Nouvelle politique RGPD',
      updatedAt: '2026-10-07T13:40:00',
    };

    service.upsert('RGPD', 'Nouvelle politique RGPD').subscribe((document) => {
      expect(document).toEqual(response);
    });

    const request = httpTesting.expectOne('/api/admin/documents/RGPD');

    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      content: 'Nouvelle politique RGPD',
    });

    request.flush(response);
  });
});
