import { Component, computed, inject, signal } from '@angular/core';

import { AnonymizationApi } from '../../../core/privacy/anonymization-api';
import { Column } from '../../../shared/ui/data-table/column';
import { DataTable, DataTableColumn } from '../../../shared/ui/data-table/data-table';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { Pagination } from '../../../shared/ui/pagination/pagination';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-anonymization-admin-page',
  imports: [Column, DataTable, EmptyState, ErrorState, LoadingState, Pagination],
  templateUrl: './anonymization-admin-page.html',
  styleUrl: './anonymization-admin-page.css',
})
export class AnonymizationAdminPage {
  private readonly anonymizationApi = inject(AnonymizationApi);

  protected readonly page = signal(0);
  protected readonly pageSize = PAGE_SIZE;

  protected readonly columns: DataTableColumn[] = [
    { key: 'lastName', header: 'Nom' },
    { key: 'firstName', header: 'Prénom' },
    { key: 'email', header: 'Email' },
    { key: 'postalAddress', header: 'Adresse postale' },
    { key: 'clubs', header: 'Clubs affiliés' },
    { key: 'phone', header: 'Téléphone' },
  ];

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
}
