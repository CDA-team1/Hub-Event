import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ClubApi } from '../../../core/clubs/club-api';
import { Confirmation } from '../../../core/dialog/confirmation';
import { ClubDto } from '../../../domain/club.model';
import { ActionButton } from '../../../shared/ui/action-button/action-button';
import { CategoryBadge } from '../../../shared/ui/category-badge/category-badge';
import { Column } from '../../../shared/ui/data-table/column';
import { DataTable, DataTableColumn } from '../../../shared/ui/data-table/data-table';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { Pagination } from '../../../shared/ui/pagination/pagination';

const PAGE_SIZE = 20;

@Component({
  imports: [
    ActionButton,
    CategoryBadge,
    Column,
    DataTable,
    EmptyState,
    ErrorState,
    LoadingState,
    Pagination,
    RouterLink,
  ],
  selector: 'app-clubs-list-page',
  styleUrl: './clubs-list-page.css',
  templateUrl: './clubs-list-page.html',
})
export class ClubsListPage {
  private readonly clubApi = inject(ClubApi);
  private readonly confirmation = inject(Confirmation);

  protected readonly page = signal(0);
  protected readonly pageSize = PAGE_SIZE;
  protected readonly deleteError = signal('');

  protected readonly columns: DataTableColumn[] = [
    { key: 'name', header: 'Nom' },
    { key: 'category', header: 'Catégorie' },
    { key: 'address', header: 'Adresse' },
    { key: 'email', header: 'Email' },
    { key: 'phone', header: 'Téléphone' },
    { key: 'actions', header: 'Actions' },
  ];

  private readonly clubs = this.clubApi.list(computed(() => ({ page: this.page(), size: this.pageSize })));

  protected readonly isLoading = this.clubs.isLoading;
  protected readonly error = this.clubs.error;
  protected readonly content = computed(() => (this.clubs.hasValue() ? this.clubs.value().content : []));
  protected readonly totalElements = computed(() =>
    this.clubs.hasValue() ? this.clubs.value().totalElements : 0,
  );

  protected onPageChanged(page: number): void {
    this.page.set(page);
  }

  protected reload(): void {
    this.clubs.reload();
  }

  protected async deleteClub(club: ClubDto): Promise<void> {
    this.deleteError.set('');
    const confirmed = await this.confirmation.confirm(
      `Supprimer le club « ${club.name} » ? Ses évènements à venir déjà publiés seront annulés, ` +
        'et ses organisateurs sans autre club seront rétrogradés en membres.',
      'Supprimer',
      'Annuler',
    );
    if (!confirmed) {
      return;
    }
    try {
      await this.clubApi.delete(club.id);
      this.clubs.reload();
    } catch {
      this.deleteError.set('La suppression a échoué, réessayez.');
    }
  }
}
