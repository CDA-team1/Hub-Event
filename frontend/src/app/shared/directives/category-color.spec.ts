import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Category } from '../../core/events/event.model';
import { CategoryColor } from './category-color';

@Component({
  imports: [CategoryColor],
  template: `<span [appCategoryColor]="category()">badge</span>`,
})
class Host {
  readonly category = signal<Category>('SPORT');
}

describe('CategoryColor', () => {
  it('applique la couleur de la catégorie et la met à jour', async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const span = (fixture.nativeElement as HTMLElement).querySelector('span')!;

    expect(span.style.getPropertyValue('--category-color')).toBe('#d2694a');
    expect(span.style.getPropertyValue('--category-bg')).toBe('rgb(210 105 74 / 12%)');
    expect(span.dataset['category']).toBe('SPORT');

    fixture.componentInstance.category.set('CULTURE');
    await fixture.whenStable();
    expect(span.style.getPropertyValue('--category-color')).toBe('#2892c3');
  });
});
