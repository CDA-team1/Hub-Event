import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { AccountApi } from '../../../core/accounts/account-api';

@Component({
  selector: 'app-activate-account-page',
  imports: [],
  templateUrl: './activate-account-page.html',
  styleUrl: './activate-account-page.css',
})
export class ActivateAccountPage {
  private readonly route = inject(ActivatedRoute);
  private readonly accountApi = inject(AccountApi);

  readonly loading = signal(true);
  readonly success = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.loading.set(false);
      this.errorMessage.set("Le lien d'activation est invalide.");
      return;
    }

    this.accountApi.activateAccount(token).subscribe({
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

        this.errorMessage.set("Une erreur est survenue lors de l'activation du compte.");
      },
    });
  }
}
