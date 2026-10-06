import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { API_URL } from '../../../core/http/api-url';
import { RgpdPage } from './rgpd-page';

describe('RgpdPage', () => {
  let httpTesting: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [RgpdPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: API_URL,
          useValue: '/api',
        },
      ],
    });

    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('affiche la politique RGPD renvoyée par l’API', async () => {
    const fixture = TestBed.createComponent(RgpdPage);

    fixture.detectChanges();

    const request = httpTesting.expectOne('/api/documents/RGPD');

    expect(request.request.method).toBe('GET');

    request.flush({
      id: 2,
      type: 'RGPD',
      content: 'Politique RGPD du Hub événementiel.',
      updatedAt: '2026-10-05T12:00:00',
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Politique RGPD du Hub événementiel.');
  });

  it('affiche une erreur si la politique RGPD ne peut pas être chargée', async () => {
    const fixture = TestBed.createComponent(RgpdPage);

    fixture.detectChanges();

    const request = httpTesting.expectOne('/api/documents/RGPD');

    request.flush('Erreur', {
      status: 500,
      statusText: 'Server Error',
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Impossible de charger la politique RGPD.');
  });
});
