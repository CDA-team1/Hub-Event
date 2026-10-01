import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Category } from '../../../core/events/event.model';
import { CategoryBadge } from '../category-badge/category-badge';

export interface CardData {
  title: string;
  imageUrl?: string;
  category?: Category;
  information: string[];
  actionLabel?: string;
  link?: string | any[];
}

@Component({
  selector: 'app-card',
  imports: [CategoryBadge, RouterLink],
  templateUrl: './card.html',
  styleUrl: './card.css',
})
export class Card {
  readonly data = input.required<CardData>();

  readonly actionClicked = output<void>();

  triggerAction(event: Event): void {
    event.stopPropagation();
    this.actionClicked.emit();
  }
}
