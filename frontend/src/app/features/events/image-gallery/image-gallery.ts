import { Component, input } from '@angular/core';

import { ImageDto } from '../../../domain/event.model';

@Component({
  selector: 'app-image-gallery',
  templateUrl: './image-gallery.html',
  styleUrl: './image-gallery.css',
})
export class ImageGallery {
  readonly images = input.required<ImageDto[]>();
  readonly eventTitle = input.required<string>();
}
