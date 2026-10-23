import { TestBed } from '@angular/core/testing';

import { ImageDto } from '../../../../../domain/event.model';
import { EventImages, ImageChanges, MAX_IMAGE_SIZE_MB } from './event-images';

const IMAGES: ImageDto[] = [
  { id: 5, eventId: 1, url: 'https://i.ibb.co/a/first.webp', isPreview: true },
  { id: 6, eventId: 1, url: 'https://i.ibb.co/b/second.webp', isPreview: false },
];

const photo = (name = 'photo.png') => new File(['x'], name, { type: 'image/png' });

describe('EventImages', () => {
  const originalCreate = URL.createObjectURL;
  const originalRevoke = URL.revokeObjectURL;
  const revoke = vi.fn<(url: string) => void>();
  let previews: number;

  beforeEach(() => {
    previews = 0;
    revoke.mockClear();
    URL.createObjectURL = vi.fn(() => `blob:preview-${++previews}`);
    URL.revokeObjectURL = revoke;
  });

  afterEach(() => {
    URL.createObjectURL = originalCreate;
    URL.revokeObjectURL = originalRevoke;
  });

  async function render(inputs: Record<string, unknown> = {}) {
    const fixture = TestBed.createComponent(EventImages);

    for (const [name, value] of Object.entries(inputs)) {
      fixture.componentRef.setInput(name, value);
    }

    const emitted: ImageChanges[] = [];
    fixture.componentInstance.changed.subscribe((changes) => emitted.push(changes));
    await fixture.whenStable();

    return { fixture, element: fixture.nativeElement as HTMLElement, emitted };
  }

  async function choose(
    fixture: Awaited<ReturnType<typeof render>>['fixture'],
    element: HTMLElement,
    files: File[],
  ) {
    const picker = element.querySelector<HTMLInputElement>('input[type="file"]')!;
    Object.defineProperty(picker, 'files', { value: files, configurable: true });
    picker.dispatchEvent(new Event('change'));
    await fixture.whenStable();
  }

  const thumbnails = (element: HTMLElement) => element.querySelectorAll('.images__item');

  it("n'affiche aucune vignette sans image et propose Parcourir", async () => {
    const { element } = await render();

    expect(thumbnails(element)).toHaveLength(0);
    expect(element.querySelector('.images__browse')?.textContent?.trim()).toBe('Parcourir');
  });

  it('affiche les photos existantes', async () => {
    const { element } = await render({ images: IMAGES });

    const sources = Array.from(element.querySelectorAll('.images__item img')).map((img) =>
      img.getAttribute('src'),
    );

    expect(sources).toEqual(['https://i.ibb.co/a/first.webp', 'https://i.ibb.co/b/second.webp']);
  });

  it('retire une photo existante au clic sur la croix et le signale', async () => {
    const { fixture, element, emitted } = await render({ images: IMAGES });

    element.querySelector<HTMLButtonElement>('.images__remove')!.click();
    await fixture.whenStable();

    expect(thumbnails(element)).toHaveLength(1);
    expect(element.querySelector('.images__item img')?.getAttribute('src')).toBe(
      'https://i.ibb.co/b/second.webp',
    );
    expect(emitted).toEqual([{ added: [], removedIds: [5] }]);
  });

  it('ajoute les images choisies avec un aperçu et le signale', async () => {
    const { fixture, element, emitted } = await render();
    const file = photo('plage.png');

    await choose(fixture, element, [file]);

    expect(element.querySelector('.images__item--new img')?.getAttribute('src')).toBe(
      'blob:preview-1',
    );
    expect(emitted).toEqual([{ added: [file], removedIds: [] }]);
  });

  it("refuse un fichier qui n'est pas une image", async () => {
    const { fixture, element, emitted } = await render();

    await choose(fixture, element, [new File(['x'], 'notes.pdf', { type: 'application/pdf' })]);

    expect(element.querySelector('.images__error')?.textContent).toBe(
      "« notes.pdf » n'est pas une image.",
    );
    expect(thumbnails(element)).toHaveLength(0);
    expect(emitted).toEqual([]);
  });

  it('refuse une image trop volumineuse', async () => {
    const { fixture, element, emitted } = await render();
    const tooBig = new File([new Uint8Array(MAX_IMAGE_SIZE_MB * 1024 * 1024 + 1)], 'grosse.png', {
      type: 'image/png',
    });

    await choose(fixture, element, [tooBig]);

    expect(element.querySelector('.images__error')?.textContent).toBe(
      `« grosse.png » dépasse ${MAX_IMAGE_SIZE_MB} Mo.`,
    );
    expect(emitted).toEqual([]);
  });

  it('garde les fichiers valides quand un autre est refusé', async () => {
    const { fixture, element, emitted } = await render();
    const valid = photo('ok.png');

    await choose(fixture, element, [valid, new File(['x'], 'notes.txt', { type: 'text/plain' })]);

    expect(thumbnails(element)).toHaveLength(1);
    expect(element.querySelector('.images__error')?.textContent).toContain('notes.txt');
    expect(emitted).toEqual([{ added: [valid], removedIds: [] }]);
  });

  it('retire un fichier en attente et libère son aperçu', async () => {
    const { fixture, element, emitted } = await render();
    await choose(fixture, element, [photo()]);

    element.querySelector<HTMLButtonElement>('.images__item--new .images__remove')!.click();
    await fixture.whenStable();

    expect(thumbnails(element)).toHaveLength(0);
    expect(revoke).toHaveBeenCalledWith('blob:preview-1');
    expect(emitted.at(-1)).toEqual({ added: [], removedIds: [] });
  });

  it('libère les aperçus à la destruction du composant', async () => {
    const { fixture, element } = await render();
    await choose(fixture, element, [photo()]);

    fixture.destroy();

    expect(revoke).toHaveBeenCalledWith('blob:preview-1');
  });

  it('bloque le bloc quand il est désactivé', async () => {
    const { element } = await render({ images: IMAGES, disabled: true });

    expect(element.querySelector('fieldset')?.disabled).toBe(true);
  });
});
