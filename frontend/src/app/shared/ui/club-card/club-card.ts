import { Component, computed, input } from '@angular/core';

import { ClubCard as ClubCardModel } from '../../../core/clubs/club-card';
import { Card, CardData } from '../card/card';

@Component({
  selector: 'app-club-card',
  imports: [Card],
  templateUrl: './club-card.html',
  styleUrl: './club-card.css',
})
export class ClubCard {
  readonly club = input.required<ClubCardModel>();

  readonly cardData = computed<CardData>(() => ({
    title: this.club().name,
    category: this.club().category,
    information: [this.club().postalAddress, this.club().email, this.club().phone],
  }));
}
