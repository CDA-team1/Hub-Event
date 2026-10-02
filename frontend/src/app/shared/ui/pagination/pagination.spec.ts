import { TestBed } from '@angular/core/testing';
import { Pagination } from './pagination';

describe('Pagination', () => {
  async function render(page: number, size: number, totalElements: number) {
    const fixture = TestBed.createComponent(Pagination);
    fixture.componentRef.setInput('page', page);
    fixture.componentRef.setInput('size', size);
    fixture.componentRef.setInput('totalElements', totalElements);
    await fixture.whenStable();
    return { fixture, element: fixture.nativeElement as HTMLElement };
  }

  it("ne s'affiche pas quand tout tient sur une seule page", async () => {
    const { element } = await render(0, 20, 5);
    expect(element.querySelector('nav')).toBeNull();
  });

  it('affiche la page courante, le total de pages et le nombre d’éléments', async () => {
    const { element } = await render(1, 20, 45);
    expect(element.textContent).toContain('Page 2 sur 3 · 45 élément(s)');
  });

  it('désactive Précédent sur la première page, Suivant sur la dernière', async () => {
    const first = await render(0, 20, 45);
    expect(first.element.querySelector('button')?.disabled).toBe(true);

    const last = await render(2, 20, 45);
    const buttons = last.element.querySelectorAll('button');
    expect(buttons[1].disabled).toBe(true);
  });

  it('émet le numéro de page (0-indexé) au clic sur Suivant puis Précédent', async () => {
    const { fixture, element } = await render(1, 20, 45);
    const emitted: number[] = [];
    fixture.componentInstance.pageChanged.subscribe((page) => emitted.push(page));

    const [previousButton, nextButton] = Array.from(element.querySelectorAll('button'));
    nextButton.click();
    previousButton.click();

    expect(emitted).toEqual([2, 0]);
  });
});
