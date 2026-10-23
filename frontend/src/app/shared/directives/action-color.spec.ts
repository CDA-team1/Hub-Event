import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActionLevel } from '../../domain/action-level';
import { ActionColor } from './action-color';

@Component({
  imports: [ActionColor],
  template: `<span [appActionColor]="level()">action</span>`,
})
class Host {
  readonly level = signal<ActionLevel>('primary');
}

describe('ActionColor', () => {
  it("applique la couleur du niveau d'action et la met à jour", async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const span = (fixture.nativeElement as HTMLElement).querySelector('span')!;

    expect(span.style.getPropertyValue('--action-color')).toBe('#2563eb');
    expect(span.style.getPropertyValue('--action-bg')).toBe('rgb(37 99 235 / 12%)');
    expect(span.dataset['actionLevel']).toBe('primary');

    fixture.componentInstance.level.set('danger');
    await fixture.whenStable();
    expect(span.style.getPropertyValue('--action-color')).toBe('#d33f3f');
  });
});
