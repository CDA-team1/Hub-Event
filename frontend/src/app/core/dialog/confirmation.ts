import { ApplicationRef, EnvironmentInjector, Service, createComponent, inject } from '@angular/core';
import { ConfirmationDialog } from '../../shared/ui/confirmation-dialog/confirmation-dialog';

/**
 * Dialogue de confirmation générique (annulation d'évènement, suppression, fin d'affiliation…).
 * Usage : `if (await confirmation.confirm('Annuler cet évènement ?')) { ... }` — la promesse ne
 * se résout qu'au clic sur un bouton (ou Échap, équivalent à Annuler).
 */
@Service()
export class Confirmation {
  private readonly appRef = inject(ApplicationRef);
  private readonly environmentInjector = inject(EnvironmentInjector);

  confirm(message: string, confirmLabel = 'Confirmer', cancelLabel = 'Annuler'): Promise<boolean> {
    const componentRef = createComponent(ConfirmationDialog, {
      environmentInjector: this.environmentInjector,
    });
    componentRef.setInput('message', message);
    componentRef.setInput('confirmLabel', confirmLabel);
    componentRef.setInput('cancelLabel', cancelLabel);
    componentRef.changeDetectorRef.detectChanges();

    document.body.append(componentRef.location.nativeElement);
    this.appRef.attachView(componentRef.hostView);

    return new Promise<boolean>((resolve) => {
      componentRef.instance.resolved.subscribe((confirmed) => {
        this.appRef.detachView(componentRef.hostView);
        componentRef.destroy();
        resolve(confirmed);
      });
      componentRef.instance.open();
    });
  }
}
