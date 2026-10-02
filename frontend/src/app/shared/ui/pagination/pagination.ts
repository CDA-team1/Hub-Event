import { Component, computed, input, output } from '@angular/core';

/**
 * Pagination pour une liste paginée par le back (`PageDto`). `page` est 0-indexé, comme le DTO.
 * Se masque d'elle-même s'il n'y a qu'une seule page.
 */
@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.html',
  styleUrl: './pagination.css',
})
export class Pagination {
  readonly page = input.required<number>();
  readonly size = input.required<number>();
  readonly totalElements = input.required<number>();

  readonly pageChanged = output<number>();

  protected readonly totalPages = computed(() =>
    Math.max(Math.ceil(this.totalElements() / this.size()), 1),
  );
  protected readonly isFirst = computed(() => this.page() <= 0);
  protected readonly isLast = computed(() => this.page() >= this.totalPages() - 1);

  protected previous(): void {
    this.goTo(this.page() - 1);
  }

  protected next(): void {
    this.goTo(this.page() + 1);
  }

  private goTo(page: number): void {
    if (page < 0 || page > this.totalPages() - 1) {
      return;
    }
    this.pageChanged.emit(page);
  }
}
