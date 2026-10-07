import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Confirmation } from '../../../core/dialog/confirmation';
import { EventApi } from '../../../core/events/event-api';
import { Category } from '../../../domain/category';
import { EventStatus, OrganizerEventDto } from '../../../domain/event.model';
import { ActionButton } from '../../../shared/ui/action-button/action-button';
import { CategoryBadge } from '../../../shared/ui/category-badge/category-badge';
import { Column } from '../../../shared/ui/data-table/column';
import { DataTable, DataTableColumn, DataTableSort } from '../../../shared/ui/data-table/data-table';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { EventStatusBadge } from '../../../shared/ui/event-status-badge/event-status-badge';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { Pagination } from '../../../shared/ui/pagination/pagination';

const PAGE_SIZE = 10;

const CATEGORY_ORDER: Record<Category, number> = { CULTURE: 0, LEISURE: 1, SPORT: 2 };
const STATUS_ORDER: Record<EventStatus, number> = { DRAFT: 0, PUBLISHED: 1, CANCELLED: 2, FINISHED: 3 };

/** Message métier renvoyé par le back (ex. « Cet événement ne peut pas être publié… »), sinon générique. */
function actionErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse && typeof error.error === 'string') {
    return error.error;
  }
  return "L'action a échoué, réessayez.";
}

type EventComparator = (a: OrganizerEventDto, b: OrganizerEventDto) => number;

const SORT_COMPARATORS: Record<string, EventComparator> = {
  dates: (a, b) => a.startDateTime.localeCompare(b.startDateTime),
  category: (a, b) => CATEGORY_ORDER[a.category] - CATEGORY_ORDER[b.category],
  status: (a, b) => STATUS_ORDER[a.status] - STATUS_ORDER[b.status],
};

@Component({
  selector: 'app-my-events-page',
  imports: [
    ActionButton,
    CategoryBadge,
    Column,
    DataTable,
    DatePipe,
    EmptyState,
    ErrorState,
    EventStatusBadge,
    LoadingState,
    Pagination,
    RouterLink,
  ],
  templateUrl: './my-events-page.html',
  styleUrl: './my-events-page.css',
})
export class MyEventsPage {
  private readonly eventApi = inject(EventApi);
  private readonly confirmation = inject(Confirmation);

  protected readonly page = signal(0);
  protected readonly pageSize = PAGE_SIZE;
  protected readonly actionError = signal('');
  protected readonly sort = signal<DataTableSort | null>(null);

  protected readonly columns: DataTableColumn[] = [
    { key: 'title', header: 'Titre' },
    { key: 'category', header: 'Catégorie', sortable: true },
    { key: 'dates', header: 'Dates', sortable: true },
    { key: 'status', header: 'Statut', sortable: true },
    { key: 'seats', header: 'Places' },
    { key: 'actions', header: 'Actions' },
  ];

  protected readonly trackByEventId = (event: OrganizerEventDto) => event.id;

  private readonly events = this.eventApi.mine();

  protected readonly isLoading = this.events.isLoading;
  protected readonly error = this.events.error;
  protected readonly content = computed(() => (this.events.hasValue() ? this.events.value() : []));
  protected readonly totalElements = computed(() => this.content().length);

  protected readonly sortedEvents = computed(() => {
    const sort = this.sort();
    const comparator = sort ? SORT_COMPARATORS[sort.key] : undefined;
    if (!sort || !comparator) {
      return this.content();
    }
    const sorted = [...this.content()].sort(comparator);
    return sort.direction === 'asc' ? sorted : sorted.reverse();
  });

  protected readonly pagedEvents = computed(() => {
    const start = this.page() * this.pageSize;
    return this.sortedEvents().slice(start, start + this.pageSize);
  });

  protected onPageChanged(page: number): void {
    this.page.set(page);
  }

  protected onSortChanged(key: string): void {
    this.sort.update((current) =>
      current?.key === key
        ? { key, direction: current.direction === 'asc' ? 'desc' : 'asc' }
        : { key, direction: 'asc' },
    );
    this.page.set(0);
  }

  protected reload(): void {
    this.events.reload();
  }

  protected async publish(event: OrganizerEventDto): Promise<void> {
    const confirmed = await this.confirmation.confirm(
      `Publier l'évènement « ${event.title} » ? Il sera visible par tous les utilisateurs.`,
      'Publier',
      'Annuler',
    );
    if (!confirmed) {
      return;
    }
    await this.runAction(() => this.eventApi.publish(event.id));
  }

  protected async finish(event: OrganizerEventDto): Promise<void> {
    const confirmed = await this.confirmation.confirm(
      `Terminer l'évènement « ${event.title} » ? Cette action est définitive : seules ses images resteront modifiables.`,
      'Terminer',
      'Annuler',
    );
    if (!confirmed) {
      return;
    }
    await this.runAction(() => this.eventApi.finish(event.id));
  }

  protected async cancel(event: OrganizerEventDto): Promise<void> {
    const confirmed = await this.confirmation.confirm(
      `Annuler l'évènement « ${event.title} » ? Les inscrits en seront informés par email.`,
      "Oui, annuler l'évènement",
      'Non, revenir',
    );
    if (!confirmed) {
      return;
    }
    await this.runAction(() => this.eventApi.cancel(event.id));
  }

  protected async deleteEvent(event: OrganizerEventDto): Promise<void> {
    const confirmed = await this.confirmation.confirm(
      `Supprimer définitivement l'évènement « ${event.title} » ? Cette action est irréversible.`,
      'Supprimer',
      'Annuler',
    );
    if (!confirmed) {
      return;
    }
    await this.runAction(() => this.eventApi.delete(event.id));
  }

  private async runAction(action: () => Promise<unknown>): Promise<void> {
    this.actionError.set('');
    try {
      await action();
      this.events.reload();
    } catch (error) {
      this.actionError.set(actionErrorMessage(error));
    }
  }
}
