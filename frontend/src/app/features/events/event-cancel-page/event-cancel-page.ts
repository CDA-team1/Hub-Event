import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventApi, isValidEventId } from '../../../core/events/event-api';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';

function cancelErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse && typeof error.error === 'string') {
    return error.error;
  }
  return "L'annulation a échoué, réessayez.";
}

/** Page de validation de l'annulation d'un évènement publié avec inscrits (maquette 14, CU20). */
@Component({
  selector: 'app-event-cancel-page',
  imports: [ErrorState, LoadingState],
  templateUrl: './event-cancel-page.html',
  styleUrl: './event-cancel-page.css',
})
export class EventCancelPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly eventApi = inject(EventApi);

  private readonly eventId = signal(Number(this.route.snapshot.paramMap.get('id')));
  private readonly event = this.eventApi.eventDetail(this.eventId);

  protected readonly invalidId = !isValidEventId(this.eventId());
  protected readonly isLoading = this.event.isLoading;
  protected readonly error = this.event.error;
  protected readonly title = computed(() => (this.event.hasValue() ? this.event.value().title : ''));
  protected readonly ready = computed(() => this.event.hasValue());

  protected readonly submitting = signal(false);
  protected readonly submitError = signal('');

  protected async confirm(): Promise<void> {
    this.submitError.set('');
    this.submitting.set(true);
    try {
      await this.eventApi.cancel(this.eventId());
      await this.router.navigate(['/mes-evenements']);
    } catch (error) {
      this.submitError.set(cancelErrorMessage(error));
    } finally {
      this.submitting.set(false);
    }
  }

  protected async cancel(): Promise<void> {
    await this.router.navigate(['/mes-evenements']);
  }
}
