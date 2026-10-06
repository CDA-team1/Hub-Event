import { TestBed } from '@angular/core/testing';
import { ClubDto } from '../../../../domain/club.model';
import { EventForm, EventFormValue } from './event-form';

function makeClub(id: number, name: string): ClubDto {
  return {
    id,
    name,
    category: 'SPORT',
    postalAddress: '1 rue de Test',
    email: 'club@test.fr',
    phone: '0600000000',
    validityEndDate: null,
    members: [],
  };
}

describe('EventForm', () => {
  async function render(options: { initialValue?: EventFormValue; clubs?: ClubDto[] } = {}) {
    const fixture = TestBed.createComponent(EventForm);
    if (options.initialValue) {
      fixture.componentRef.setInput('initialValue', options.initialValue);
    }
    if (options.clubs) {
      fixture.componentRef.setInput('clubs', options.clubs);
    }
    await fixture.whenStable();
    return { fixture, element: fixture.nativeElement as HTMLElement };
  }

  async function type(element: HTMLElement, selector: string, value: string): Promise<void> {
    const field = element.querySelector<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>(
      selector,
    )!;
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('change'));
    field.dispatchEvent(new Event('blur'));
    await new Promise((resolve) => setTimeout(resolve));
  }

  function submitForm(element: HTMLElement): void {
    element.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
  }

  async function fillRequiredFields(element: HTMLElement): Promise<void> {
    await type(element, '#title', 'Concert');
    await type(element, '#category', 'CULTURE');
    await type(element, '#description', 'Un bel évènement');
    await type(element, '#location', 'Paris');
    await type(element, '#maxSeats', '100');
    await type(element, '#startDateTime', '2026-12-01T20:00');
    await type(element, '#affiliatedPrice', '10');
    await type(element, '#nonAffiliatedPrice', '15');
  }

  it("n'émet rien et affiche les erreurs si le formulaire est vide (création)", async () => {
    const { fixture, element } = await render({ clubs: [makeClub(1, 'Club A')] });
    const emitted: EventFormValue[] = [];
    fixture.componentInstance.submitted.subscribe((value) => emitted.push(value));

    submitForm(element);
    await new Promise((resolve) => setTimeout(resolve));

    expect(emitted).toEqual([]);
    expect(element.textContent).toContain('Le titre est obligatoire.');
    expect(element.textContent).toContain('Le club organisateur est obligatoire.');
  });

  it('refuse un nombre de places à zéro et un tarif négatif', async () => {
    const { fixture, element } = await render({ clubs: [makeClub(1, 'Club A')] });
    const emitted: EventFormValue[] = [];
    fixture.componentInstance.submitted.subscribe((value) => emitted.push(value));

    await fillRequiredFields(element);
    await type(element, '#maxSeats', '0');
    await type(element, '#affiliatedPrice', '-5');
    await type(element, '#clubId', '1');

    submitForm(element);
    await new Promise((resolve) => setTimeout(resolve));

    expect(emitted).toEqual([]);
    expect(element.textContent).toContain('Le nombre de places doit être supérieur à zéro.');
    expect(element.textContent).toContain('Le tarif affilié doit être supérieur ou égal à zéro.');
  });

  it('émet la valeur saisie en création, avec le club choisi', async () => {
    const { fixture, element } = await render({ clubs: [makeClub(1, 'Club A')] });
    const emitted: EventFormValue[] = [];
    fixture.componentInstance.submitted.subscribe((value) => emitted.push(value));

    await fillRequiredFields(element);
    await type(element, '#clubId', '1');

    submitForm(element);
    await new Promise((resolve) => setTimeout(resolve));

    expect(emitted).toEqual([
      {
        title: 'Concert',
        category: 'CULTURE',
        description: 'Un bel évènement',
        location: 'Paris',
        maxSeats: 100,
        startDateTime: '2026-12-01T20:00',
        endDateTime: '',
        affiliatedPrice: 10,
        nonAffiliatedPrice: 15,
        clubId: '1',
      },
    ]);
  });

  it("n'affiche pas le champ Club et n'exige pas de club en modification", async () => {
    const { fixture, element } = await render();
    const emitted: EventFormValue[] = [];
    fixture.componentInstance.submitted.subscribe((value) => emitted.push(value));

    expect(element.querySelector('#clubId')).toBeNull();

    await fillRequiredFields(element);
    submitForm(element);
    await new Promise((resolve) => setTimeout(resolve));

    expect(emitted).toHaveLength(1);
  });

  it('pré-remplit les champs depuis initialValue', async () => {
    const { element } = await render({
      initialValue: {
        title: 'Évènement existant',
        category: 'SPORT',
        description: 'Description existante',
        location: 'Lyon',
        maxSeats: 50,
        startDateTime: '2026-11-01T18:00',
        endDateTime: '2026-11-01T20:00',
        affiliatedPrice: 5,
        nonAffiliatedPrice: 8,
        clubId: '',
      },
    });

    expect(element.querySelector<HTMLInputElement>('#title')?.value).toBe('Évènement existant');
    expect(element.querySelector<HTMLSelectElement>('#category')?.value).toBe('SPORT');
    expect(element.querySelector<HTMLInputElement>('#maxSeats')?.value).toBe('50');
  });

  it('émet cancelled au clic sur Annuler', async () => {
    const { fixture, element } = await render();
    const emitted: void[] = [];
    fixture.componentInstance.cancelled.subscribe(() => emitted.push(undefined));

    element.querySelector<HTMLButtonElement>('button[type="button"]')!.click();

    expect(emitted).toHaveLength(1);
  });

  it('désactive les champs et le bouton Valider quand locked est vrai', async () => {
    const { fixture, element } = await render({
      initialValue: {
        title: 'Évènement terminé',
        category: 'SPORT',
        description: 'Description',
        location: 'Lyon',
        maxSeats: 50,
        startDateTime: '2026-01-01T18:00',
        endDateTime: '',
        affiliatedPrice: 5,
        nonAffiliatedPrice: 8,
        clubId: '',
      },
    });
    fixture.componentRef.setInput('locked', true);
    await fixture.whenStable();

    expect(element.querySelector<HTMLInputElement>('#title')?.disabled).toBe(true);
    expect(element.querySelector<HTMLButtonElement>('button[type="submit"]')?.disabled).toBe(true);
    expect(element.textContent).toContain(
      "Cet évènement est terminé : ses informations ne sont plus modifiables.",
    );
  });
});
