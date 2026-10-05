import { TestBed } from '@angular/core/testing';
import { ClubFormRequest } from '../../../../domain/club.model';
import { ClubForm } from './club-form';

describe('ClubForm', () => {
  async function render(initialValue?: ClubFormRequest) {
    const fixture = TestBed.createComponent(ClubForm);
    if (initialValue) {
      fixture.componentRef.setInput('initialValue', initialValue);
    }
    await fixture.whenStable();
    return { fixture, element: fixture.nativeElement as HTMLElement };
  }

  async function type(element: HTMLElement, selector: string, value: string): Promise<void> {
    const field = element.querySelector<HTMLInputElement | HTMLSelectElement>(selector)!;
    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('change'));
    field.dispatchEvent(new Event('blur'));
    await new Promise((resolve) => setTimeout(resolve));
  }

  function submitForm(element: HTMLElement): void {
    element.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
  }

  it("n'émet rien et affiche les erreurs si le formulaire est vide", async () => {
    const { fixture, element } = await render();
    const emitted: ClubFormRequest[] = [];
    fixture.componentInstance.submitted.subscribe((value) => emitted.push(value));

    submitForm(element);
    await new Promise((resolve) => setTimeout(resolve));

    expect(emitted).toEqual([]);
    expect(element.textContent).toContain('Le nom du club est obligatoire.');
    expect(element.textContent).toContain('La catégorie du club est obligatoire.');
  });

  it('émet la valeur saisie quand le formulaire est valide', async () => {
    const { fixture, element } = await render();
    const emitted: ClubFormRequest[] = [];
    fixture.componentInstance.submitted.subscribe((value) => emitted.push(value));

    await type(element, '#name', 'Club de boxe');
    await type(element, '#category', 'SPORT');
    await type(element, '#postalAddress', '1 rue de Paris');
    await type(element, '#email', 'club@test.fr');
    await type(element, '#phone', '0600000000');

    submitForm(element);
    await new Promise((resolve) => setTimeout(resolve));

    expect(emitted).toEqual([
      {
        name: 'Club de boxe',
        category: 'SPORT',
        postalAddress: '1 rue de Paris',
        email: 'club@test.fr',
        phone: '0600000000',
      },
    ]);
  });

  it('pré-remplit les champs depuis initialValue', async () => {
    const { element } = await render({
      name: 'Club existant',
      category: 'CULTURE',
      postalAddress: '2 rue de Lyon',
      email: 'existant@test.fr',
      phone: '0700000000',
    });

    expect(element.querySelector<HTMLInputElement>('#name')?.value).toBe('Club existant');
    expect(element.querySelector<HTMLSelectElement>('#category')?.value).toBe('CULTURE');
    expect(element.querySelector<HTMLInputElement>('#postalAddress')?.value).toBe('2 rue de Lyon');
  });

  it("émet cancelled au clic sur Annuler", async () => {
    const { fixture, element } = await render();
    const emitted: void[] = [];
    fixture.componentInstance.cancelled.subscribe(() => emitted.push(undefined));

    element.querySelector<HTMLButtonElement>('button[type="button"]')!.click();

    expect(emitted).toHaveLength(1);
  });
});
