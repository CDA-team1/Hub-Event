import { Component, computed, inject } from '@angular/core';
import { Carousel } from '../../../shared/ui/carousel/carousel';
import { EventCard } from '../../../shared/ui/event-card/event-card';
import { EventApi } from '../../../core/events/event-api';

@Component({
  imports: [Carousel, EventCard],
  selector: 'app-events-home-page',
  styleUrl: './events-home-page.css',
  templateUrl: './events-home-page.html',
})
export class EventsHomePage {
  protected readonly eventApi = inject(EventApi);

  protected readonly cultureEvents = computed(() => this.eventApi.publicEvents.value().cultureEvents);

  protected readonly sportEvents = computed(() => this.eventApi.publicEvents.value().sportEvents);

  protected readonly leisureEvents = computed(() => this.eventApi.publicEvents.value().leisureEvents);

  protected readonly pastEvents = computed(() => this.eventApi.publicEvents.value().pastEvents);
}
