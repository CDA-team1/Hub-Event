import { Component, computed, inject, signal } from '@angular/core';

import { AdminAnonymizationDto } from '../../../domain/anonymization.model';
import { AnonymizationApi } from '../../../core/privacy/anonymization-api';
import { Column } from '../../../shared/ui/data-table/column';
import { DataTable, DataTableColumn } from '../../../shared/ui/data-table/data-table';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { Pagination } from '../../../shared/ui/pagination/pagination';
import { Confirmation } from '../../../core/dialog/confirmation';
import { ActionButton } from '../../../shared/ui/action-button/action-button';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-anonymization-admin-page',
  imports: [ActionButton, Column, DataTable, EmptyState, ErrorState, LoadingState, Pagination],
  templateUrl: './anonymization-admin-page.html',
  styleUrl: './anonymization-admin-page.css',
})
export class AnonymizationAdminPage {
  private readonly anonymizationApi = inject(AnonymizationApi);
  private readonly confirmation = inject(Confirmation);

  protected readonly actionError = signal('');

  protected readonly page = signal(0);
  protected readonly pageSize = PAGE_SIZE;

  protected readonly columns: DataTableColumn[] = [
    { key: 'lastName', header: 'Nom' },
    { key: 'firstName', header: 'Prénom', hideOnSmall: true },
    { key: 'email', header: 'Email', hideOnSmall: true },
    { key: 'postalAddress', header: 'Adresse postale', hideOnSmall: true },
    { key: 'clubs', header: 'Clubs affiliés', hideOnSmall: true },
    { key: 'phone', header: 'Téléphone', hideOnSmall: true },
    { key: 'actions', header: 'Actions' },
  ];

  protected clubNames(request: AdminAnonymizationDto): string {
    return request.user.clubs.length === 0
      ? 'Aucun'
      : request.user.clubs.map((club) => club.name).join(', ');
  }

  private readonly requests = this.anonymizationApi.listPending(
    computed(() => ({
      page: this.page(),
      size: this.pageSize,
    })),
  );

  protected readonly isLoading = this.requests.isLoading;
  protected readonly error = this.requests.error;
  protected readonly content = computed(() =>
    this.requests.hasValue() ? this.requests.value().content : [],
  );
  protected readonly totalElements = computed(() =>
    this.requests.hasValue() ? this.requests.value().totalElements : 0,
  );

  protected onPageChanged(page: number): void {
    this.page.set(page);
  }

  protected reload(): void {
    this.requests.reload();
  }

  protected async validateRequest(request: AdminAnonymizationDto): Promise<void> {
    this.actionError.set('');

    const confirmed = await this.confirmation.confirm(
      `Valider la demande d'anonymisation de ${request.user.firstName} ${request.user.lastName} ? Cette action est irréversible.`,
      'Valider',
      'Annuler',
    );

    if (!confirmed) {
      return;
    }

    try {
      await this.anonymizationApi.validate(request.id);
      this.requests.reload();
    } catch {
      this.actionError.set("La validation de la demande d'anonymisation a échoué. Réessayez.");
    }
  }
}
