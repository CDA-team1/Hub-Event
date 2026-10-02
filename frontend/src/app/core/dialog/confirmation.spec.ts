import { TestBed } from '@angular/core/testing';
import { Confirmation } from './confirmation';

describe('Confirmation', () => {
  let confirmation: Confirmation;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    confirmation = TestBed.inject(Confirmation);
  });

  afterEach(() => {
    document.querySelectorAll('dialog').forEach((dialog) => dialog.remove());
  });

  it('résout à true quand on clique sur Confirmer', async () => {
    const result = confirmation.confirm('Continuer ?');

    document.querySelector<HTMLButtonElement>('dialog .confirm')!.click();

    expect(await result).toBe(true);
  });

  it('résout à false quand on clique sur Annuler', async () => {
    const result = confirmation.confirm('Continuer ?');

    document.querySelector<HTMLButtonElement>('dialog .cancel')!.click();

    expect(await result).toBe(false);
  });

  it('affiche le message et les libellés personnalisés', () => {
    void confirmation.confirm('Supprimer ce club ?', 'Supprimer', 'Garder');

    const dialog = document.querySelector('dialog')!;
    expect(dialog.textContent).toContain('Supprimer ce club ?');
    expect(dialog.querySelector('.confirm')?.textContent).toContain('Supprimer');
    expect(dialog.querySelector('.cancel')?.textContent).toContain('Garder');

    dialog.querySelector<HTMLButtonElement>('.cancel')!.click();
  });

  it('retire le dialogue du DOM après résolution', async () => {
    const result = confirmation.confirm('Continuer ?');
    document.querySelector<HTMLButtonElement>('dialog .confirm')!.click();
    await result;

    expect(document.querySelector('dialog')).toBeNull();
  });
});
