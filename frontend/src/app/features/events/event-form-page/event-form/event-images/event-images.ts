import { Component, DestroyRef, computed, inject, input, output, signal } from '@angular/core';

import { ImageDto } from '../../../../../domain/event.model';

/** Taille maximale d'une image, alignée sur la limite configurée côté back. */
export const MAX_IMAGE_SIZE_MB = 5;

/** Modifications de la galerie en attente, appliquées au clic sur Valider du formulaire. */
export interface ImageChanges {
  added: File[];
  removedIds: number[];
}

interface PendingImage {
  file: File;
  previewUrl: string;
}

@Component({
  selector: 'app-event-images',
  templateUrl: './event-images.html',
  styleUrl: './event-images.css',
})
export class EventImages {
  readonly images = input<ImageDto[]>([]);
  readonly disabled = input(false);

  readonly changed = output<ImageChanges>();

  protected readonly maxSizeMb = MAX_IMAGE_SIZE_MB;
  protected readonly pending = signal<PendingImage[]>([]);
  protected readonly error = signal('');

  private readonly removedIds = signal<number[]>([]);

  protected readonly keptImages = computed(() =>
    this.images().filter((image) => !this.removedIds().includes(image.id)),
  );

  constructor() {
    inject(DestroyRef).onDestroy(() =>
      this.pending().forEach((item) => URL.revokeObjectURL(item.previewUrl)),
    );
  }

  protected onFilesSelected(event: Event): void {
    const picker = event.target as HTMLInputElement;
    const files = Array.from(picker.files ?? []);
    picker.value = '';

    const accepted: PendingImage[] = [];
    const rejected: string[] = [];

    for (const file of files) {
      if (!file.type.startsWith('image/')) {
        rejected.push(`« ${file.name} » n'est pas une image.`);
      } else if (file.size > MAX_IMAGE_SIZE_MB * 1024 * 1024) {
        rejected.push(`« ${file.name} » dépasse ${MAX_IMAGE_SIZE_MB} Mo.`);
      } else {
        accepted.push({ file, previewUrl: URL.createObjectURL(file) });
      }
    }

    this.error.set(rejected.join(' '));

    if (accepted.length > 0) {
      this.pending.update((current) => [...current, ...accepted]);
      this.emitChanges();
    }
  }

  protected removeExisting(imageId: number): void {
    this.removedIds.update((ids) => [...ids, imageId]);
    this.emitChanges();
  }

  protected removePending(item: PendingImage): void {
    URL.revokeObjectURL(item.previewUrl);
    this.pending.update((current) => current.filter((other) => other !== item));
    this.emitChanges();
  }

  private emitChanges(): void {
    this.changed.emit({
      added: this.pending().map((item) => item.file),
      removedIds: this.removedIds(),
    });
  }
}
