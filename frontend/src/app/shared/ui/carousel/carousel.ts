import {
  Component,
  contentChild,
  ElementRef,
  input,
  linkedSignal,
  output,
  TemplateRef,
  viewChildren,
} from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';

@Component({
  imports: [NgTemplateOutlet],
  selector: 'app-carousel',
  styleUrl: './carousel.css',
  templateUrl: './carousel.html',
  host: {
    '(keydown.arrowleft)': 'show(index() - 1)',
    '(keydown.arrowright)': 'show(index() + 1)',
  },
})
export class Carousel<T> {
  readonly items = input.required<T[]>();
  readonly renderItem = contentChild.required<TemplateRef<{ $implicit: T }>>(TemplateRef);
  readonly indexChanged = output<number>();

  private readonly slides = viewChildren<ElementRef<HTMLElement>>('slide');

  /** Position courante - reste sur le dernier index valide si items() se réduit */
  protected readonly index = linkedSignal<T[], number>({
    source: this.items,
    computation: (items, previous) => Math.min(previous?.value ?? 0, Math.max(items.length - 1, 0)),
  });

  /** Affiche l'élément à la position donnée - boucle aux extrémités */
  protected show(index: number): void {
    const count = this.items().length;
    if (count === 0) return;
    this.index.set((index + count) % count);
    this.slides()[this.index()]?.nativeElement.scrollIntoView?.({
      behavior: 'smooth',
      inline: 'start',
      block: 'nearest',
    });
    this.indexChanged.emit(this.index());
  }
}
