import { Component, computed, input, output } from '@angular/core';

import { EventCardDto } from '../../../domain/event.model';
import { Card, CardData } from '../card/card';

@Component({
  selector: 'app-event-card',
  imports: [Card],
  templateUrl: './event-card.html',
  styleUrl: './event-card.css',
})
export class EventCard {
  readonly event = input.required<EventCardDto>();
  readonly showActions = input(false);

  readonly registerToggled = output<number>();

  readonly cardData = computed<CardData>(() => ({
    title: this.event().title,
    category: this.event().category,
    information: [
      this.event().startDateTime,
      this.event().location,
      `Tarif affilié : ${this.event().affiliatedPrice} €`,
      `Tarif non affilié : ${this.event().nonAffiliatedPrice} €`,
    ],
    actionLabel: this.showActions() ? "S'inscrire" : undefined,
  }));

  toggleRegistration(): void {
    this.registerToggled.emit(this.event().id);
  }
}
