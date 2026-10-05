import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { API_URL } from '../../../core/http/api-url';
import { CguPage } from './cgu-page';

describe('CguPage', () => {
  let httpTesting: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [CguPage],
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

  it('affiche les CGU renvoyées par l’API', async () => {
    const fixture = TestBed.createComponent(CguPage);

    fixture.detectChanges();

    const request = httpTesting.expectOne('/api/documents/CGU');

    expect(request.request.method).toBe('GET');

    request.flush({
      id: 1,
      type: 'CGU',
      content: 'Conditions générales d’utilisation du Hub événementiel.',
      updatedAt: '2026-10-05T12:00:00',
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Conditions générales d’utilisation du Hub événementiel.',
    );
  });

  it('affiche une erreur si les CGU ne peuvent pas être chargées', async () => {
    const fixture = TestBed.createComponent(CguPage);

    fixture.detectChanges();

    const request = httpTesting.expectOne('/api/documents/CGU');

    request.flush('Erreur', {
      status: 500,
      statusText: 'Server Error',
    });

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Impossible de charger les Conditions Générales d’Utilisation.',
    );
  });
});
