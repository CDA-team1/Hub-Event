import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ErrorState } from './error-state';

@Component({
  imports: [ErrorState],
  template: `
    <app-error-state>
      <p>Impossible de charger les données.</p>
      <button actions type="button">Réessayer</button>
    </app-error-state>
  `,
})
class Host {}

describe('ErrorState', () => {
  it('affiche le message en alerte et projette les actions', async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    const message = element.querySelector('.message');
    expect(message?.getAttribute('role')).toBe('alert');
    expect(message?.textContent).toContain('Impossible de charger les données.');
    expect(element.querySelector('.actions button')?.textContent).toContain('Réessayer');
  });
});
