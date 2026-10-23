import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EventApi } from './event-api';

describe('EventApi', () => {
  let service: EventApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EventApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('images', () => {
    it('envoie les fichiers en multipart dans le champ « files »', async () => {
      const first = new File(['a'], 'a.png', { type: 'image/png' });
      const second = new File(['b'], 'b.png', { type: 'image/png' });
      const created = [{ id: 1, eventId: 7, url: 'https://i.ibb.co/a.webp', isPreview: true }];

      const result = service.addImages(7, [first, second]);

      const request = http.expectOne('/api/events/7/images');
      expect(request.request.method).toBe('POST');
      expect(request.request.body).toBeInstanceOf(FormData);
      const sent = (request.request.body as FormData).getAll('files') as File[];
      expect(sent.map((file) => file.name)).toEqual(['a.png', 'b.png']);
      request.flush(created);

      await expect(result).resolves.toEqual(created);
    });

    it('retire une image de la galerie', async () => {
      const result = service.removeImage(7, 5);

      const request = http.expectOne('/api/events/7/images/5');
      expect(request.request.method).toBe('DELETE');
      request.flush(null, { status: 204, statusText: 'No Content' });

      await expect(result).resolves.toBeUndefined();
    });
  });
});
