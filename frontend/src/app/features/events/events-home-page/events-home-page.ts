import { Component, computed, inject } from '@angular/core';
import { Carousel } from './carousel/carousel';
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
  private readonly publicEvents = this.eventApi.publicEvents();

  protected readonly isLoading = this.publicEvents.isLoading;
  protected readonly error = this.publicEvents.error;

  protected readonly cultureEvents = computed(() =>
    this.publicEvents.hasValue() ? this.publicEvents.value().cultureEvents : [],
  );

  protected readonly sportEvents = computed(() =>
    this.publicEvents.hasValue() ? this.publicEvents.value().sportEvents : [],
  );

  protected readonly leisureEvents = computed(() =>
    this.publicEvents.hasValue() ? this.publicEvents.value().leisureEvents : [],
  );

  protected readonly pastEvents = computed(() =>
    this.publicEvents.hasValue() ? this.publicEvents.value().pastEvents : [],
  );

  reload(): void {
    this.publicEvents.reload();
  }
}
