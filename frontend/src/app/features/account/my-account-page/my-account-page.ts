import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AccountApi } from '../../../core/accounts/account-api';
import { Auth } from '../../../core/auth/auth';
import { UserProfileDto } from '../../../domain/account.model';
import { UpdateUserRequest } from '../../../domain/update-user-request';
import { AccountForm } from './account-form/account-form';

@Component({
  selector: 'app-my-account-page',
  imports: [AccountForm, RouterLink],
  templateUrl: './my-account-page.html',
  styleUrl: './my-account-page.css',
})
export class MyAccountPage {
  private readonly accountApi = inject(AccountApi);
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  readonly profile = signal<UserProfileDto | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly emailError = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  constructor() {
    this.loadProfile();
  }

  update(request: UpdateUserRequest): void {
    if (this.saving()) {
      return;
    }

    const currentProfile = this.profile();

    if (!currentProfile) {
      return;
    }

    const emailChanged = request.email !== currentProfile.email;

    this.saving.set(true);
    this.errorMessage.set(null);
    this.emailError.set(null);
    this.successMessage.set(null);

    this.accountApi.updateOwnAccount(request).subscribe({
      next: (account) => {
        this.saving.set(false);

        if (emailChanged) {
          this.auth.logout();
          void this.router.navigate(['/connexion']);
          return;
        }

        this.profile.update((profile) =>
          profile
            ? {
                ...profile,
                ...account,
              }
            : profile,
        );

        this.successMessage.set(
          request.password
            ? 'Vos informations ont été mises à jour. Un email de confirmation a été envoyé pour le changement de mot de passe.'
            : 'Vos informations ont été mises à jour.',
        );
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);

        if (error.status === 400 && typeof error.error === 'string') {
          if (error.error.toLowerCase().includes('email')) {
            this.emailError.set(error.error);
            return;
          }

          this.errorMessage.set(error.error);
          return;
        }

        this.errorMessage.set('Une erreur est survenue lors de la modification du compte.');
      },
    });
  }

  cancel(): void {
    void this.router.navigate(['/']);
  }

  private loadProfile(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.accountApi.getOwnAccount().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);

        if (error.status === 400 && typeof error.error === 'string') {
          this.errorMessage.set(error.error);
          return;
        }

        this.errorMessage.set('Impossible de charger les informations de votre compte.');
      },
    });
  }
}
