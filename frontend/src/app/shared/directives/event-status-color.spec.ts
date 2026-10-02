import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { EventStatus } from '../../domain/event.model';
import { EventStatusColor } from './event-status-color';

@Component({
  imports: [EventStatusColor],
  template: `<span [appEventStatusColor]="status()">badge</span>`,
})
class Host {
  readonly status = signal<EventStatus>('PUBLISHED');
}

describe('EventStatusColor', () => {
  it('applique la couleur du statut et la met à jour', async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const span = (fixture.nativeElement as HTMLElement).querySelector('span')!;

    expect(span.style.getPropertyValue('--status-color')).toBe('#2e9c55');
    expect(span.style.getPropertyValue('--status-bg')).toBe('rgb(46 156 85 / 12%)');
    expect(span.dataset['status']).toBe('PUBLISHED');

    fixture.componentInstance.status.set('CANCELLED');
    await fixture.whenStable();
    expect(span.style.getPropertyValue('--status-color')).toBe('#d33f3f');
  });
});
