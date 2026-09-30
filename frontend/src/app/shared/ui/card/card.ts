import { Component, input, output } from '@angular/core';

export interface CardData {
  title: string;
  imageUrl?: string;
  category?: string;
  information: string[];
  actionLabel?: string;
}

@Component({
  selector: 'app-card',
  imports: [],
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
