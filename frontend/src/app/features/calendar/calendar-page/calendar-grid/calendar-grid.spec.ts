import { TestBed } from '@angular/core/testing';
import { buildCalendarWeeks } from '../../../../domain/calendar-rules';
import { CalendarGrid } from './calendar-grid';

describe('CalendarGrid', () => {
  async function render(highlighted: readonly string[]) {
    const fixture = TestBed.createComponent(CalendarGrid);
    fixture.componentRef.setInput('weeks', buildCalendarWeeks('2026-10-05', '2026-10-11'));
    fixture.componentRef.setInput('highlightedDates', new Set(highlighted));
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('affiche une cellule par jour de la période', async () => {
    const element = await render([]);
    expect(element.querySelectorAll('.day')).toHaveLength(7);
  });

  it('met en surbrillance uniquement les jours donnés', async () => {
    const element = await render(['2026-10-07']);
    expect(element.querySelectorAll('.day--highlighted')).toHaveLength(1);
  });
});
