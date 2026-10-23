import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Confirmation } from '../../../core/dialog/confirmation';
import { AnonymizationApi } from '../../../core/privacy/anonymization-api';

@Component({
  selector: 'app-anonymization-request-page',
  templateUrl: './anonymization-request-page.html',
  styleUrl: './anonymization-request-page.css',
})
export class AnonymizationRequestPage {
  private readonly anonymizationApi = inject(AnonymizationApi);
  private readonly confirmation = inject(Confirmation);
  private readonly router = inject(Router);

  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  protected async submit(): Promise<void> {
    if (this.submitting() || this.successMessage()) {
      return;
    }

    const confirmed = await this.confirmation.confirm(
      "Confirmer votre demande d'anonymisation ? Elle devra ensuite être validée par un administrateur.",
      'Valider la demande',
      'Annuler',
    );

    if (!confirmed) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    try {
      await this.anonymizationApi.createRequest();

      this.successMessage.set(
        "Votre demande d'anonymisation a bien été enregistrée. Elle doit maintenant être validée par un administrateur.",
      );
    } catch (error) {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 400 &&
        typeof error.error === 'string'
      ) {
        this.errorMessage.set(error.error);
      } else {
        this.errorMessage.set(
          "Impossible d'enregistrer votre demande d'anonymisation. Veuillez réessayer.",
        );
      }
    } finally {
      this.submitting.set(false);
    }
  }

  protected cancel(): void {
    void this.router.navigate(['/']);
  }
}
