import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubApi } from '../../../core/clubs/club-api';
import { EventApi } from '../../../core/events/event-api';
import { Category } from '../../../domain/category';
import { CreateEventRequest, EventStatus, UpdateEventRequest } from '../../../domain/event.model';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { EventForm, EventFormValue } from './event-form/event-form';

function submitErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse && typeof error.error === 'string') {
    return error.error;
  }
  return "L'enregistrement a échoué.";
}

@Component({
  selector: 'app-event-form-page',
  imports: [ErrorState, EventForm, LoadingState],
  templateUrl: './event-form-page.html',
  styleUrl: './event-form-page.css',
})
export class EventFormPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly eventApi = inject(EventApi);
  private readonly clubApi = inject(ClubApi);

  private readonly idParam = this.route.snapshot.paramMap.get('id');
  protected readonly isEditMode = this.idParam !== null;
  private readonly eventId = signal(Number(this.idParam ?? 0));

  private readonly event = this.isEditMode ? this.eventApi.eventDetail(this.eventId) : undefined;
  private readonly clubs = this.isEditMode ? undefined : this.clubApi.mine();

  protected readonly isLoading = computed(
    () => (this.event?.isLoading() ?? false) || (this.clubs?.isLoading() ?? false),
  );
  protected readonly error = computed(() => this.event?.error() ?? this.clubs?.error());
  protected readonly ready = computed(
    () =>
      (!this.isEditMode || (this.event?.hasValue() ?? false)) &&
      (this.isEditMode || (this.clubs?.hasValue() ?? false)),
  );

  protected readonly availableClubs = computed(() =>
    this.clubs?.hasValue() ? this.clubs.value() : undefined,
  );

  protected readonly status = computed<EventStatus | undefined>(() =>
    this.event?.hasValue() ? this.event.value().status : undefined,
  );
  protected readonly locked = computed(() => this.status() === 'FINISHED');

  protected readonly initialValue = computed<EventFormValue | undefined>(() => {
    if (!this.isEditMode || !this.event?.hasValue()) {
      return undefined;
    }
    const detail = this.event.value();
    return {
      title: detail.title,
      description: detail.description,
      location: detail.location,
      category: detail.category,
      startDateTime: detail.startDateTime.slice(0, 16),
      endDateTime: detail.endDateTime?.slice(0, 16) ?? '',
      affiliatedPrice: detail.affiliatedPrice,
      nonAffiliatedPrice: detail.nonAffiliatedPrice,
      maxSeats: detail.maxSeats,
      clubId: '',
    };
  });

  protected readonly submitError = signal('');

  protected async onSubmitted(value: EventFormValue): Promise<void> {
    this.submitError.set('');
    try {
      if (this.isEditMode) {
        const request: UpdateEventRequest = {
          title: value.title,
          description: value.description,
          location: value.location,
          startDateTime: value.startDateTime,
          endDateTime: value.endDateTime || null,
          affiliatedPrice: value.affiliatedPrice as number,
          nonAffiliatedPrice: value.nonAffiliatedPrice as number,
          maxSeats: value.maxSeats as number,
          category: value.category as Category,
        };
        await this.eventApi.update(this.eventId(), request);
      } else {
        const request: CreateEventRequest = {
          title: value.title,
          description: value.description,
          location: value.location,
          startDateTime: value.startDateTime,
          endDateTime: value.endDateTime || null,
          affiliatedPrice: value.affiliatedPrice as number,
          nonAffiliatedPrice: value.nonAffiliatedPrice as number,
          maxSeats: value.maxSeats as number,
          category: value.category as Category,
          clubId: Number(value.clubId),
        };
        await this.eventApi.create(request);
      }
      await this.router.navigate(['/mes-evenements']);
    } catch (error) {
      this.submitError.set(submitErrorMessage(error));
    }
  }

  protected async onCancelled(): Promise<void> {
    await this.router.navigate(['/mes-evenements']);
  }
}
