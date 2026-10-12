import { Component, input } from '@angular/core';

import { ImageDto } from '../../../../domain/event.model';

@Component({
  selector: 'app-image-gallery',
  templateUrl: './image-gallery.html',
  styleUrl: './image-gallery.css',
})
export class ImageGallery {
  readonly images = input.required<ImageDto[]>();
  readonly eventTitle = input.required<string>();
  /** Illustration affichée à la place des photos, tant que l'évènement n'en a aucune. */
  readonly placeholderUrl = input<string | null>(null);
}
