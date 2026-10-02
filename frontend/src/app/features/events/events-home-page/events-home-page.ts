import { Component, computed, inject } from '@angular/core';
import { Carousel } from '../../../shared/ui/carousel/carousel';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { EventCard } from '../../../shared/ui/event-card/event-card';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { EventApi } from '../../../core/events/event-api';

@Component({
  imports: [Carousel, EventCard, EmptyState, ErrorState, LoadingState],
  selector: 'app-events-home-page',
  styleUrl: './events-home-page.css',
  templateUrl: './events-home-page.html',
})
export class EventsHomePage {
  private readonly eventApi = inject(EventApi);

  protected readonly isLoading = this.eventApi.publicEvents.isLoading;
  protected readonly error = this.eventApi.publicEvents.error;

  protected readonly cultureEvents = computed(() =>
    this.eventApi.publicEvents.hasValue() ? this.eventApi.publicEvents.value().cultureEvents : [],
  );

  protected readonly sportEvents = computed(() =>
    this.eventApi.publicEvents.hasValue() ? this.eventApi.publicEvents.value().sportEvents : [],
  );

  protected readonly leisureEvents = computed(() =>
    this.eventApi.publicEvents.hasValue() ? this.eventApi.publicEvents.value().leisureEvents : [],
  );

  protected readonly pastEvents = computed(() =>
    this.eventApi.publicEvents.hasValue() ? this.eventApi.publicEvents.value().pastEvents : [],
  );

  reload(): void {
    this.eventApi.publicEvents.reload();
  }
}
