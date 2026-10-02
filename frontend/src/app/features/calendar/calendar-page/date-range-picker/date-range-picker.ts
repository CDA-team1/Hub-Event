import { Component, input, output } from '@angular/core';
import { DateRange } from '../../../../core/calendar/calendar-api';

/** Deux champs de date, n'émet que lorsque la plage est valide (from ≤ to). */
@Component({
  selector: 'app-date-range-picker',
  templateUrl: './date-range-picker.html',
  styleUrl: './date-range-picker.css',
})
export class DateRangePicker {
  readonly from = input.required<string>();
  readonly to = input.required<string>();

  readonly rangeChanged = output<DateRange>();

  protected onFromChanged(value: string): void {
    if (value && value <= this.to()) {
      this.rangeChanged.emit({ from: value, to: this.to() });
    }
  }

  protected onToChanged(value: string): void {
    if (value && this.from() <= value) {
      this.rangeChanged.emit({ from: this.from(), to: value });
    }
  }
}
