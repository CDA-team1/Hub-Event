import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { LoadingState } from './loading-state';

@Component({
  imports: [LoadingState],
  template: `<app-loading-state>Chargement des événements…</app-loading-state>`,
})
class Host {}

describe('LoadingState', () => {
  it('affiche le message projeté en tant que statut', async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    const status = element.querySelector('[role="status"]');
    expect(status?.textContent).toContain('Chargement des événements…');
  });
});
