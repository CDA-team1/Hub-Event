import { Component, computed, input, output } from '@angular/core';

import { EventDetailResponse } from '../../../domain/event.model';

@Component({
  selector: 'app-owner-actions',
  templateUrl: './owner-actions.html',
  styleUrl: './owner-actions.css',
})
export class OwnerActions {
  readonly event = input.required<EventDetailResponse>();
  readonly pending = input(false);
  readonly errorMessage = input('');

  readonly editClicked = output<void>();
  readonly publishClicked = output<void>();
  readonly finishClicked = output<void>();
  readonly cancelClicked = output<void>();
  readonly deleteClicked = output<void>();

  protected readonly isDraft = computed(() => this.event().status === 'DRAFT');
  protected readonly isPublished = computed(() => this.event().status === 'PUBLISHED');
  protected readonly hasRegistrations = computed(
    () => this.event().maxSeats - this.event().remainingSeats > 0,
  );
}
