import { TestBed } from '@angular/core/testing';

import { ImageDto } from '../../../../domain/event.model';
import { ImageGallery } from './image-gallery';

describe('ImageGallery', () => {
  const images: ImageDto[] = [
    { id: 1, eventId: 10, url: 'https://i.ibb.co/a/preview.webp', isPreview: true },
    { id: 2, eventId: 10, url: 'https://i.ibb.co/b/second.webp', isPreview: false },
  ];

  async function render(value: ImageDto[], placeholderUrl: string | null = null) {
    const fixture = TestBed.createComponent(ImageGallery);
    fixture.componentRef.setInput('images', value);
    fixture.componentRef.setInput('placeholderUrl', placeholderUrl);
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

  it('affiche le badge « Photo principale » uniquement sur la preview', async () => {
    const element = await render(images);

    const badges = element.querySelectorAll('.gallery__badge');
    expect(badges).toHaveLength(1);
    expect(badges[0].textContent?.trim()).toBe('Photo principale');
    const preview = element.querySelector('.gallery__item--preview');
    expect(preview?.querySelector('.gallery__badge')).toBe(badges[0]);
  });

  it("affiche l'illustration par défaut avec la pastille « Photo à venir » sans aucune photo", async () => {
    const element = await render([], 'placeholders/sport.webp');

    expect(element.querySelectorAll('img')).toHaveLength(1);
    expect(element.querySelector('img')?.getAttribute('src')).toBe('placeholders/sport.webp');
    expect(element.querySelector('.gallery__badge')?.textContent?.trim()).toBe('Photo à venir');
  });

  it("n'affiche pas l'illustration par défaut quand il y a des photos", async () => {
    const element = await render(images, 'placeholders/sport.webp');

    expect(element.querySelector('img[src="placeholders/sport.webp"]')).toBeNull();
    expect(element.querySelector('.gallery__badge')?.textContent?.trim()).toBe('Photo principale');
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
