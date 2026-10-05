import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { CalendarApi, DateRange } from '../../../core/calendar/calendar-api';
import { Confirmation } from '../../../core/dialog/confirmation';
import { RegistrationApi } from '../../../core/registrations/registration-api';
import {
  addDays,
  addMonths,
  buildCalendarWeeks,
  daysInRange,
  endOfMonth,
  endOfWeek,
  shiftRange,
  startOfMonth,
  startOfWeek,
  toIsoDate,
} from '../../../domain/calendar-rules';
import { EventCardDto } from '../../../domain/event.model';
import { ActionButton } from '../../../shared/ui/action-button/action-button';
import { Column } from '../../../shared/ui/data-table/column';
import { DataTable, DataTableColumn } from '../../../shared/ui/data-table/data-table';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { Pagination } from '../../../shared/ui/pagination/pagination';
import { CalendarGrid } from './calendar-grid/calendar-grid';
import { DateRangePicker } from './date-range-picker/date-range-picker';

type ViewMode = 'SEMAINE' | 'MOIS' | 'PERIODE';

const PAGE_SIZE = 10;

function weekRange(date: Date): DateRange {
  return { from: toIsoDate(startOfWeek(date)), to: toIsoDate(endOfWeek(date)) };
}

function monthRange(date: Date): DateRange {
  return { from: toIsoDate(startOfMonth(date)), to: toIsoDate(endOfMonth(date)) };
}

@Component({
  selector: 'app-calendar-page',
  imports: [
    DatePipe,
    ActionButton,
    CalendarGrid,
    Column,
    DataTable,
    DateRangePicker,
    EmptyState,
    ErrorState,
    LoadingState,
    Pagination,
  ],
  templateUrl: './calendar-page.html',
  styleUrl: './calendar-page.css',
})
export class CalendarPage {
  private readonly calendarApi = inject(CalendarApi);
  private readonly registrationApi = inject(RegistrationApi);
  private readonly confirmation = inject(Confirmation);

  protected readonly mode = signal<ViewMode>('SEMAINE');
  private readonly anchor = signal(new Date());
  private readonly customRange = signal<DateRange>(weekRange(new Date()));

  protected readonly page = signal(0);
  protected readonly pageSize = PAGE_SIZE;
  protected readonly unregisterError = signal('');

  protected readonly columns: DataTableColumn[] = [
    { key: 'title', header: 'Évènement' },
    { key: 'date', header: 'Date' },
    { key: 'actions', header: 'Actions' },
  ];

  protected readonly trackByEventId = (event: EventCardDto) => event.id;

  protected readonly range = computed<DateRange>(() => {
    switch (this.mode()) {
      case 'SEMAINE':
        return weekRange(this.anchor());
      case 'MOIS':
        return monthRange(this.anchor());
      case 'PERIODE':
        return this.customRange();
    }
  });

  private readonly calendar = this.calendarApi.myCalendar(this.range);

  protected readonly isLoading = this.calendar.isLoading;
  protected readonly error = this.calendar.error;
  protected readonly events = computed(() =>
    this.calendar.hasValue() ? this.calendar.value() : [],
  );

  protected readonly pagedEvents = computed(() => {
    const start = this.page() * this.pageSize;
    return this.events().slice(start, start + this.pageSize);
  });

  protected readonly weeks = computed(() => buildCalendarWeeks(this.range().from, this.range().to));
  protected readonly highlightedDates = computed(
    () => new Set(this.pagedEvents().map((event) => event.startDateTime.slice(0, 10))),
  );

  protected setMode(mode: ViewMode): void {
    this.mode.set(mode);
    this.page.set(0);
  }

  protected previous(): void {
    this.shift(-1);
  }

  protected next(): void {
    this.shift(1);
  }

  protected onRangeChanged(range: DateRange): void {
    this.customRange.set(range);
    this.page.set(0);
  }

  protected onPageChanged(page: number): void {
    this.page.set(page);
  }

  protected reload(): void {
    this.calendar.reload();
  }

  protected async unregister(eventId: number, title: string): Promise<void> {
    this.unregisterError.set('');
    const confirmed = await this.confirmation.confirm(
      `Se désinscrire de « ${title} » ?`,
      'Se désinscrire',
      'Annuler',
    );
    if (!confirmed) {
      return;
    }
    try {
      await this.registrationApi.unregister(eventId);
      this.page.set(0);
      this.calendar.reload();
    } catch {
      this.unregisterError.set('La désinscription a échoué, réessayez.');
    }
  }

  private shift(direction: 1 | -1): void {
    if (this.mode() === 'PERIODE') {
      const { from, to } = this.customRange();
      this.customRange.set(shiftRange(from, to, direction * daysInRange(from, to)));
    } else if (this.mode() === 'SEMAINE') {
      this.anchor.update((date) => addDays(date, direction * 7));
    } else {
      this.anchor.update((date) => addMonths(date, direction));
    }
    this.page.set(0);
  }
}
