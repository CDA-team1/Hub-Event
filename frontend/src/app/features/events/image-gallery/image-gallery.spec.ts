import { TestBed } from '@angular/core/testing';

import { ImageDto } from '../../../domain/event.model';
import { ImageGallery } from './image-gallery';

describe('ImageGallery', () => {
  const images: ImageDto[] = [
    { id: 1, eventId: 10, url: 'https://i.ibb.co/a/preview.webp', isPreview: true },
    { id: 2, eventId: 10, url: 'https://i.ibb.co/b/second.webp', isPreview: false },
  ];

  async function render(value: ImageDto[]) {
    const fixture = TestBed.createComponent(ImageGallery);
    fixture.componentRef.setInput('images', value);
    fixture.componentRef.setInput('eventTitle', 'Tournoi de futsal');
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('affiche une image par photo, dans l’ordre reçu', async () => {
    const element = await render(images);

    const sources = Array.from(element.querySelectorAll('img')).map((img) =>
      img.getAttribute('src'),
    );
    expect(sources).toEqual(['https://i.ibb.co/a/preview.webp', 'https://i.ibb.co/b/second.webp']);
  });

  it('repère la preview', async () => {
    const element = await render(images);

    const preview = element.querySelectorAll('.gallery__item--preview');
    expect(preview).toHaveLength(1);
    expect(preview[0].querySelector('img')?.getAttribute('src')).toBe(
      'https://i.ibb.co/a/preview.webp',
    );
  });

  it("décrit chaque photo avec le titre de l'événement", async () => {
    const element = await render(images);

    expect(element.querySelector('img')?.getAttribute('alt')).toBe('Photo 1 – Tournoi de futsal');
  });

  it("n'affiche rien quand il n'y a aucune photo", async () => {
    const element = await render([]);

    expect(element.querySelector('section')).toBeNull();
  });
});
