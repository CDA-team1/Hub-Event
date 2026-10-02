import { Component, input, output } from '@angular/core';
import { Category } from '../../../domain/category';
import { CategoryBadge } from '../category-badge/category-badge';

export interface CardData {
  title: string;
  imageUrl?: string;
  category?: Category;
  information: string[];
  actionLabel?: string;
}

@Component({
  selector: 'app-card',
  imports: [CategoryBadge],
  templateUrl: './card.html',
  styleUrl: './card.css',
})
export class Card {
  readonly data = input.required<CardData>();

  readonly actionClicked = output<void>();

  triggerAction(): void {
    this.actionClicked.emit();
  }
}
