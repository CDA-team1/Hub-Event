import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';

import { EventApi } from '../../../core/events/event-api';
import { criteriaFromParams, criteriaToParams } from '../../../domain/event-search';
import { EventSearchCriteria } from '../../../domain/event.model';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { EventCard } from '../../../shared/ui/event-card/event-card';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { SearchForm } from '../search-form/search-form';

@Component({
  selector: 'app-event-search-page',
  imports: [EmptyState, ErrorState, EventCard, LoadingState, SearchForm],
  styleUrl: './event-search-page.css',
  templateUrl: './event-search-page.html',
})
export class EventSearchPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly eventApi = inject(EventApi);

  private readonly queryParams = toSignal(this.route.queryParamMap, {
    initialValue: this.route.snapshot.queryParamMap,
  });

  /** Les critères vivent dans l'URL : la page n'en garde aucune copie. */
  protected readonly criteria = computed(() => criteriaFromParams(this.queryParams()));
  protected readonly results = this.eventApi.search(this.criteria);

  protected search(criteria: EventSearchCriteria): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: criteriaToParams(criteria),
    });
  }
}
