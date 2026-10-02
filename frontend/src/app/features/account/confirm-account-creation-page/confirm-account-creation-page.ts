import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { AccountApi } from '../../../core/accounts/account-api';
import { ConfirmAccountCreationRequest } from '../../../domain/confirm-account-creation-request';
import { ConfirmAccountForm } from './confirm-account-form/confirm-account-form';

@Component({
  selector: 'app-confirm-account-creation-page',
  imports: [ConfirmAccountForm],
  templateUrl: './confirm-account-creation-page.html',
  styleUrl: './confirm-account-creation-page.css',
})
export class ConfirmAccountCreationPage {
  private readonly route = inject(ActivatedRoute);
  private readonly accountApi = inject(AccountApi);
  private readonly token = this.route.snapshot.queryParamMap.get('token');
  readonly invalidLink = !this.token;

  readonly loading = signal(false);
  readonly success = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    if (!this.token) {
      this.errorMessage.set('Le lien de confirmation est invalide.');
    }
  }

  confirm(request: ConfirmAccountCreationRequest): void {
    if (!this.token || this.loading()) {
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.accountApi.confirmAccountCreation(this.token, request).subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set(true);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);

        if (error.status === 400 && typeof error.error === 'string') {
          this.errorMessage.set(error.error);
          return;
        }

        this.errorMessage.set('Une erreur est survenue lors de la confirmation du compte.');
      },
    });
  }
}
