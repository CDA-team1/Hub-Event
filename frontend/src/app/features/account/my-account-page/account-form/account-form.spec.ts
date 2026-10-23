import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserProfileDto } from '../../../../domain/account.model';
import { AccountForm } from './account-form';

describe('AccountForm', () => {
  let fixture: ComponentFixture<AccountForm>;
  let component: AccountForm;
  let element: HTMLElement;

  const profile: UserProfileDto = {
    id: 1,
    lastName: 'Doe',
    firstName: 'John',
    postalAddress: '1 rue de Test',
    email: 'john.doe@test.com',
    phone: '0600000000',
    status: 'ACTIVE',
    role: 'MEMBER',
    clubs: [{ id: 1, name: 'Club de Test' }],
  };

  const stable = () => fixture.detectChanges();

  beforeEach(async () => {
    fixture = TestBed.createComponent(AccountForm);

    fixture.componentRef.setInput('profile', profile);

    component = fixture.componentInstance;
    element = fixture.nativeElement as HTMLElement;

    await stable();
  });

  async function type(selector: string, value: string): Promise<void> {
    const field = element.querySelector<HTMLInputElement>(selector)!;

    field.value = value;
    field.dispatchEvent(new Event('input'));
    field.dispatchEvent(new Event('blur'));

    await stable();
  }

  function submitForm(): void {
    element.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
  }

  it('préremplit le formulaire avec le profil', () => {
    expect(component.form.controls.lastName.value).toBe('Doe');
    expect(component.form.controls.firstName.value).toBe('John');
    expect(component.form.controls.postalAddress.value).toBe('1 rue de Test');
    expect(component.form.controls.email.value).toBe('john.doe@test.com');
    expect(component.form.controls.phone.value).toBe('0600000000');

    expect(element.textContent).toContain('Club de Test');
  });

  it('refuse les champs obligatoires vides', async () => {
    await type('#lastName', '');
    await type('#firstName', '');
    await type('#postalAddress', '');
    await type('#email', '');

    submitForm();
    await stable();

    expect(component.form.invalid).toBe(true);
    expect(element.textContent).toContain('Le nom est obligatoire.');
    expect(element.textContent).toContain('Le prénom est obligatoire.');
    expect(element.textContent).toContain("L'adresse postale est obligatoire.");
    expect(element.textContent).toContain("L'email est obligatoire.");
  });

  it('autorise un mot de passe vide', async () => {
    const emit = vi.spyOn(component.submitted, 'emit');

    submitForm();
    await stable();

    expect(component.form.valid).toBe(true);
    expect(emit).toHaveBeenCalledWith({
      lastName: 'Doe',
      firstName: 'John',
      postalAddress: '1 rue de Test',
      email: 'john.doe@test.com',
      phone: '0600000000',
      password: null,
    });
  });

  it('refuse un mot de passe faible', async () => {
    await type('#password', 'faible');

    submitForm();
    await stable();

    expect(component.form.invalid).toBe(true);
    expect(element.textContent).toContain('Le mot de passe doit contenir au moins 12 caractères.');
  });

  it('émet le nouveau mot de passe lorsqu’il est valide', async () => {
    const emit = vi.spyOn(component.submitted, 'emit');

    await type('#password', 'NouveauSecret123!');

    submitForm();
    await stable();

    expect(component.form.valid).toBe(true);
    expect(emit).toHaveBeenCalledWith({
      lastName: 'Doe',
      firstName: 'John',
      postalAddress: '1 rue de Test',
      email: 'john.doe@test.com',
      phone: '0600000000',
      password: 'NouveauSecret123!',
    });
  });

  it('émet cancelled lorsque l’utilisateur clique sur Annuler', () => {
    const emit = vi.spyOn(component.cancelled, 'emit');

    component.cancel();

    expect(emit).toHaveBeenCalled();
  });
});
