import { TestBed } from '@angular/core/testing';
import { DateRangePicker } from './date-range-picker';

describe('DateRangePicker', () => {
  async function render() {
    const fixture = TestBed.createComponent(DateRangePicker);
    fixture.componentRef.setInput('from', '2026-10-05');
    fixture.componentRef.setInput('to', '2026-10-11');
    await fixture.whenStable();
    return { fixture, element: fixture.nativeElement as HTMLElement };
  }

  it('émet la nouvelle plage quand la date de début change et reste valide', async () => {
    const { fixture, element } = await render();
    const emitted: unknown[] = [];
    fixture.componentInstance.rangeChanged.subscribe((range) => emitted.push(range));

    const fromInput = element.querySelectorAll<HTMLInputElement>('input')[0];
    fromInput.value = '2026-10-03';
    fromInput.dispatchEvent(new Event('change'));

    expect(emitted).toEqual([{ from: '2026-10-03', to: '2026-10-11' }]);
  });

  it("n'émet rien si la date de début dépasse la date de fin", async () => {
    const { fixture, element } = await render();
    const emitted: unknown[] = [];
    fixture.componentInstance.rangeChanged.subscribe((range) => emitted.push(range));

    const fromInput = element.querySelectorAll<HTMLInputElement>('input')[0];
    fromInput.value = '2026-10-20';
    fromInput.dispatchEvent(new Event('change'));

    expect(emitted).toEqual([]);
  });
});
