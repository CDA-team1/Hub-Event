import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { EmptyState } from './empty-state';

@Component({
  imports: [EmptyState],
  template: `
    <app-empty-state>
      <p>Aucun élément.</p>
      @if (withAction) {
        <button actions type="button">Recharger</button>
      }
    </app-empty-state>
  `,
})
class Host {
  withAction = false;
}

describe('EmptyState', () => {
  it('affiche le contenu projeté par défaut', async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('.message')?.textContent).toContain('Aucun élément.');
    expect(element.querySelector('.actions button')).toBeNull();
  });

  it('projette les actions dans le slot [actions] quand il y en a', async () => {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.withAction = true;
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('.actions button')?.textContent).toContain('Recharger');
  });
});
