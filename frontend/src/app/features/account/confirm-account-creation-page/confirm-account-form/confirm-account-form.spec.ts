import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { ConfirmAccountForm } from './confirm-account-form';

describe('ConfirmAccountForm', () => {
  let component: ConfirmAccountForm;
  let element: HTMLElement;

  const stable = async () => {
    await TestBed.inject(ApplicationRef).whenStable();
  };

  beforeEach(async () => {
    const fixture = TestBed.createComponent(ConfirmAccountForm);

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

  it('affiche les erreurs de validation si les champs obligatoires sont vides', async () => {
    submitForm();
    await stable();

    expect(element.textContent).toContain('Le mot de passe temporaire est obligatoire.');
    expect(element.textContent).toContain('Le nouveau mot de passe est obligatoire.');
    expect(element.textContent).toContain('La confirmation est obligatoire.');
  });

  it('refuse un nouveau mot de passe qui ne respecte pas les critères de sécurité', async () => {
    await type('#temporaryPassword', 'Temporaire123!');
    await type('#newPassword', 'motdepasse');
    await type('#confirmPassword', 'motdepasse');

    submitForm();
    await stable();

    expect(component.form.invalid).toBe(true);
    expect(element.textContent).toContain('Le mot de passe doit contenir au moins 12 caractères.');
  });

  it('affiche une erreur lorsque les deux nouveaux mots de passe sont différents', async () => {
    await type('#temporaryPassword', 'Temporaire123!');
    await type('#newPassword', 'NouveauSecret123!');
    await type('#confirmPassword', 'AutreSecret123!');

    submitForm();
    await stable();

    expect(component.form.invalid).toBe(true);
    expect(element.textContent).toContain('Les mots de passe ne correspondent pas.');
  });

  it('émet les trois mots de passe lorsque le formulaire est valide', async () => {
    const emit = vi.spyOn(component.submitted, 'emit');

    await type('#temporaryPassword', 'Temporaire123!');
    await type('#newPassword', 'NouveauSecret123!');
    await type('#confirmPassword', 'NouveauSecret123!');

    submitForm();
    await stable();

    expect(component.form.valid).toBe(true);
    expect(emit).toHaveBeenCalledWith({
      temporaryPassword: 'Temporaire123!',
      newPassword: 'NouveauSecret123!',
      confirmPassword: 'NouveauSecret123!',
    });
  });
});
