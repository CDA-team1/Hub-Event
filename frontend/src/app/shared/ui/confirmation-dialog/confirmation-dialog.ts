import { Component, ElementRef, input, output, viewChild } from '@angular/core';

/**
 * Boîte de dialogue de confirmation, construite sur l'élément natif `<dialog>` : piège le focus,
 * se ferme sur Échap et restitue le focus à la fermeture, sans code supplémentaire.
 */
@Component({
  selector: 'app-confirmation-dialog',
  templateUrl: './confirmation-dialog.html',
  styleUrl: './confirmation-dialog.css',
})
export class ConfirmationDialog {
  readonly message = input.required<string>();
  readonly confirmLabel = input('Confirmer');
  readonly cancelLabel = input('Annuler');

  readonly resolved = output<boolean>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  open(): void {
    this.dialog().nativeElement.showModal();
  }

  protected onClose(): void {
    this.resolved.emit(this.dialog().nativeElement.returnValue === 'confirm');
  }
}
