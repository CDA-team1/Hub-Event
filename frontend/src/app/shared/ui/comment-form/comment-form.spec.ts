import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommentForm } from './comment-form';

describe('CommentForm', () => {
  let component: CommentForm;
  let fixture: ComponentFixture<CommentForm>;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommentForm],
    }).compileComponents();

    fixture = TestBed.createComponent(CommentForm);
    component = fixture.componentInstance;
    element = fixture.nativeElement;

    fixture.detectChanges();
    await fixture.whenStable();
  });

  async function typeComment(value: string): Promise<void> {
    const textarea = element.querySelector('textarea') as HTMLTextAreaElement;

    textarea.value = value;
    textarea.dispatchEvent(new Event('input'));

    fixture.detectChanges();
    await fixture.whenStable();
  }

  async function submitForm(): Promise<void> {
    const form = element.querySelector('form') as HTMLFormElement;

    form.dispatchEvent(
      new Event('submit', {
        bubbles: true,
        cancelable: true,
      }),
    );

    await fixture.whenStable();
    fixture.detectChanges();
  }

  it('émet le commentaire saisi lorsqu’il est valide', async () => {
    const emit = vi.spyOn(component.submitted, 'emit');

    await typeComment('Super événement !');
    await submitForm();

    expect(emit).toHaveBeenCalledWith('Super événement !');
  });

  it('supprime les espaces inutiles avant de publier', async () => {
    const emit = vi.spyOn(component.submitted, 'emit');

    await typeComment('  Super événement !  ');
    await submitForm();

    expect(emit).toHaveBeenCalledWith('Super événement !');
  });

  it('refuse un commentaire vide', async () => {
    const emit = vi.spyOn(component.submitted, 'emit');

    await submitForm();

    expect(emit).not.toHaveBeenCalled();
    expect(element.textContent).toContain('Veuillez saisir un commentaire.');
  });

  it('refuse un commentaire composé uniquement d’espaces', async () => {
    const emit = vi.spyOn(component.submitted, 'emit');

    await typeComment('   ');
    await submitForm();

    expect(emit).not.toHaveBeenCalled();
    expect(element.textContent).toContain('Veuillez saisir un commentaire.');
  });

  it('vide la saisie lorsque l’utilisateur clique sur Annuler', async () => {
    await typeComment('Commentaire à abandonner');

    const buttons = element.querySelectorAll('button');
    const cancelButton = Array.from(buttons).find(
      (button) => button.textContent?.trim() === 'Annuler',
    ) as HTMLButtonElement;

    cancelButton.click();

    fixture.detectChanges();
    await fixture.whenStable();

    const textarea = element.querySelector('textarea') as HTMLTextAreaElement;

    expect(textarea.value).toBe('');
  });
});
