import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AccountApi } from '../../../core/accounts/account-api';

/**
 * Page ouverte depuis l'email de confirmation d'un changement de mot de passe (CU13). Le lien
 * pointe vers le front plutôt que vers l'API : un lien d'API qui modifie des données est consommé
 * par les outils d'analyse des messageries (qui le « visitent » à l'avance) avant le clic de la personne.
 */
@Component({
  selector: 'app-confirm-password-change-page',
  imports: [RouterLink],
  templateUrl: './confirm-password-change-page.html',
  styleUrl: './confirm-password-change-page.css',
})
export class ConfirmPasswordChangePage {
  private readonly route = inject(ActivatedRoute);
  private readonly accountApi = inject(AccountApi);

  readonly loading = signal(true);
  readonly success = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.loading.set(false);
      this.errorMessage.set('Le lien de confirmation est invalide.');
      return;
    }

    this.accountApi.confirmPasswordChange(token).subscribe({
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

        this.errorMessage.set('Une erreur est survenue lors de la confirmation du mot de passe.');
      },
    });
  }
}
