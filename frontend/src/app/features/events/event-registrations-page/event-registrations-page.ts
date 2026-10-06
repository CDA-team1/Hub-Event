import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { RegistrationApi } from '../../../core/registrations/registration-api';
import { RegistrationDto } from '../../../domain/event.model';
import { ActionButton } from '../../../shared/ui/action-button/action-button';
import { Column } from '../../../shared/ui/data-table/column';
import { DataTable, DataTableColumn } from '../../../shared/ui/data-table/data-table';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';

@Component({
  selector: 'app-event-registrations-page',
  imports: [ActionButton, Column, DataTable, DatePipe, EmptyState, ErrorState, LoadingState, RouterLink],
  templateUrl: './event-registrations-page.html',
  styleUrl: './event-registrations-page.css',
})
export class EventRegistrationsPage {
  private readonly route = inject(ActivatedRoute);
  private readonly registrationApi = inject(RegistrationApi);

  private readonly eventId = signal(Number(this.route.snapshot.paramMap.get('id')));

  protected readonly columns: DataTableColumn[] = [
    { key: 'email', header: 'Email' },
    { key: 'status', header: 'Statut' },
    { key: 'date', header: "Date d'inscription" },
    { key: 'actions', header: 'Actions' },
  ];

  private readonly registrations = this.registrationApi.forEvent(this.eventId);

  protected readonly isLoading = this.registrations.isLoading;
  protected readonly error = this.registrations.error;
  protected readonly content = computed(() =>
    this.registrations.hasValue() ? this.registrations.value() : [],
  );

  /** Membre sélectionné pour désinscription : fait apparaître le champ Motif (maquette 15). */
  protected readonly selected = signal<RegistrationDto | null>(null);
  protected readonly reason = signal('');
  protected readonly reasonError = signal('');
  protected readonly submitError = signal('');

  protected reload(): void {
    this.registrations.reload();
  }

  protected startCancel(registration: RegistrationDto): void {
    this.selected.set(registration);
    this.reason.set('');
    this.reasonError.set('');
    this.submitError.set('');
  }

  protected cancelForm(): void {
    this.selected.set(null);
    this.reason.set('');
    this.reasonError.set('');
  }

  protected async confirmCancel(): Promise<void> {
    const registration = this.selected();
    if (!registration) {
      return;
    }

    const reason = this.reason().trim();
    if (!reason) {
      this.reasonError.set('Le motif est obligatoire.');
      return;
    }
    this.reasonError.set('');
    this.submitError.set('');

    try {
      await this.registrationApi.cancelByOrganizer(this.eventId(), registration.userId, reason);
      this.selected.set(null);
      this.reason.set('');
      this.registrations.reload();
    } catch {
      this.submitError.set('La désinscription a échoué, réessayez.');
    }
  }
}
