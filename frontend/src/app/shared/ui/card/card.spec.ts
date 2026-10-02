import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Card, CardData } from './card';

describe('Card', () => {
  async function render(data: CardData) {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(Card);
    fixture.componentRef.setInput('data', data);
    await fixture.whenStable();
    return { fixture, element: fixture.nativeElement as HTMLElement };
  }

  it('affiche le titre et les informations', async () => {
    const { element } = await render({
      title: 'Marathon de Paris',
      information: ['7 sept. 2026 — 8h00', 'Champs-Élysées, Paris'],
    });

    expect(element.textContent).toContain('Marathon de Paris');
    expect(element.textContent).toContain('7 sept. 2026 — 8h00');
    expect(element.textContent).toContain('Champs-Élysées, Paris');
  });

  it('affiche un badge avec le libellé français quand une catégorie est fournie', async () => {
    const { element } = await render({
      title: 'Marathon de Paris',
      information: [],
      category: 'SPORT',
    });

    expect(element.querySelector('app-category-badge')?.textContent?.trim()).toBe('Sport');
  });

  it("n'affiche aucun badge sans catégorie", async () => {
    const { element } = await render({ title: 'Marathon de Paris', information: [] });

    expect(element.querySelector('app-category-badge')).toBeNull();
  });

  it("émet actionClicked au clic sur le bouton d'action", async () => {
    const { fixture, element } = await render({
      title: 'Marathon de Paris',
      information: [],
      actionLabel: "S'inscrire",
    });
    const emitted: void[] = [];
    fixture.componentInstance.actionClicked.subscribe(() => emitted.push(undefined));

    element.querySelector<HTMLButtonElement>('.card__action')!.click();

    expect(emitted).toHaveLength(1);
  });
});
