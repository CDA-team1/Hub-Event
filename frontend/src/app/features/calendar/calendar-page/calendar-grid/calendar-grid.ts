import { Component, input } from '@angular/core';
import { CalendarWeek } from '../../../../domain/calendar-rules';

const WEEKDAY_LABELS = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

/** Grille de calendrier (semaines complètes) avec mise en valeur des jours donnés. */
@Component({
  selector: 'app-calendar-grid',
  templateUrl: './calendar-grid.html',
  styleUrl: './calendar-grid.css',
})
export class CalendarGrid {
  readonly weeks = input.required<CalendarWeek[]>();
  readonly highlightedDates = input.required<ReadonlySet<string>>();

  protected readonly weekdayLabels = WEEKDAY_LABELS;
}
