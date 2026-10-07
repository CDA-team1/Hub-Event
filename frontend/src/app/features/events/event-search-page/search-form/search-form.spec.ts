import { TestBed } from '@angular/core/testing';
import { EventSearchCriteria } from '../../../../domain/event.model';
import { SearchForm } from './search-form';

describe('SearchForm', () => {
  async function render(criteria?: EventSearchCriteria) {
    const fixture = TestBed.createComponent(SearchForm);
    if (criteria) {
      fixture.componentRef.setInput('criteria', criteria);
    }
    await fixture.whenStable();
    const emitted: EventSearchCriteria[] = [];
    fixture.componentInstance.criteriaChanged.subscribe((value) => emitted.push(value));
    return { fixture, element: fixture.nativeElement as HTMLElement, emitted };
  }

  async function type(element: HTMLElement, selector: string, value: string): Promise<void> {
    const field = element.querySelector<HTMLInputElement | HTMLSelectElement>(selector)!;
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('change'));
    field.dispatchEvent(new Event('blur'));
    await new Promise((resolve) => setTimeout(resolve));
  }

  async function submitForm(element: HTMLElement): Promise<void> {
    element.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
    await new Promise((resolve) => setTimeout(resolve));
  }

  it('affiche les sept champs de la maquette et les deux boutons', async () => {
    const { element } = await render();

    for (const id of [
      'category',
      'min-price',
      'max-price',
      'location',
      'start-date',
      'end-date',
      'keywords',
    ]) {
      expect(element.querySelector(`#search-${id}`)).not.toBeNull();
    }
    expect(element.querySelector('button[type="submit"]')?.textContent).toContain('Valider');
    expect(element.querySelector('button[type="button"]')?.textContent).toContain('Annuler');
  });

  it("pré-remplit les champs depuis les critères de l'URL", async () => {
    const { element } = await render({
      category: 'SPORT',
      minPrice: 5,
      maxPrice: 20,
      location: 'Lyon',
      startDate: '2026-10-01',
      endDate: '2026-10-31',
      keywords: 'yoga',
    });

    expect(element.querySelector<HTMLSelectElement>('#search-category')?.value).toBe('SPORT');
    expect(element.querySelector<HTMLInputElement>('#search-min-price')?.value).toBe('5');
    expect(element.querySelector<HTMLInputElement>('#search-max-price')?.value).toBe('20');
    expect(element.querySelector<HTMLInputElement>('#search-location')?.value).toBe('Lyon');
    expect(element.querySelector<HTMLInputElement>('#search-start-date')?.value).toBe('2026-10-01');
    expect(element.querySelector<HTMLInputElement>('#search-keywords')?.value).toBe('yoga');
  });

  it('émet les critères saisis, nettoyés et convertis', async () => {
    const { element, emitted } = await render();

    await type(element, '#search-category', 'LEISURE');
    await type(element, '#search-min-price', '2,5');
    await type(element, '#search-max-price', '10');
    await type(element, '#search-location', '  Toulouse  ');
    await type(element, '#search-start-date', '2026-11-01');
    await type(element, '#search-keywords', 'jeux');
    await submitForm(element);

    expect(emitted).toEqual([
      {
        category: 'LEISURE',
        minPrice: 2.5,
        maxPrice: 10,
        location: 'Toulouse',
        startDate: '2026-11-01',
        keywords: 'jeux',
      },
    ]);
  });

  it('accepte une recherche sans aucun critère', async () => {
    const { element, emitted } = await render();

    await submitForm(element);

    expect(emitted).toEqual([{}]);
  });

  it("refuse un prix qui n'est pas un nombre positif", async () => {
    const { element, emitted } = await render();

    await type(element, '#search-min-price', 'abc');
    await submitForm(element);

    expect(emitted).toEqual([]);
    expect(element.textContent).toContain('Le prix minimum doit être un nombre positif.');
  });

  it('refuse un prix maximum inférieur au prix minimum', async () => {
    const { element, emitted } = await render();

    await type(element, '#search-min-price', '20');
    await type(element, '#search-max-price', '10');
    await submitForm(element);

    expect(emitted).toEqual([]);
    expect(element.textContent).toContain(
      'Le prix maximum doit être supérieur ou égal au prix minimum.',
    );
  });

  it('refuse une date de fin antérieure à la date de début', async () => {
    const { element, emitted } = await render();

    await type(element, '#search-start-date', '2026-10-10');
    await type(element, '#search-end-date', '2026-10-05');
    await submitForm(element);

    expect(emitted).toEqual([]);
    expect(element.textContent).toContain(
      'La date de fin doit être postérieure à la date de début.',
    );
  });

  it('vide les champs et émet des critères vides au clic sur Annuler', async () => {
    const { fixture, element, emitted } = await render({ category: 'SPORT', keywords: 'yoga' });

    element.querySelector<HTMLButtonElement>('button[type="button"]')!.click();
    await fixture.whenStable();
    await new Promise((resolve) => setTimeout(resolve));

    expect(emitted).toEqual([{}]);
    expect(element.querySelector<HTMLSelectElement>('#search-category')?.value).toBe('');
    expect(element.querySelector<HTMLInputElement>('#search-keywords')?.value).toBe('');
  });
});
