import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormField, form, required, submit } from '@angular/forms/signals';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AccountApi } from '../../../core/accounts/account-api';
import { toIsoDate } from '../../../domain/calendar-rules';
import { AdminUserDto } from '../../../domain/account.model';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';

interface SuspensionDraft {
  reason: string;
  endDate: string;
}

function suspensionErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse && typeof error.error === 'string') {
    return error.error;
  }
  return 'La suspension a échoué, réessayez.';
}

/** Page de suspension d'un compte par l'administrateur (maquette 21, CU30). */
@Component({
  selector: 'app-suspend-user-page',
  imports: [FormField, ErrorState, LoadingState],
  templateUrl: './suspend-user-page.html',
  styleUrl: './suspend-user-page.css',
})
export class SuspendUserPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly accountApi = inject(AccountApi);

  private readonly userId = Number(this.route.snapshot.paramMap.get('id'));

  protected readonly invalidId = !Number.isInteger(this.userId) || this.userId <= 0;
  protected readonly user = signal<AdminUserDto | null>(null);
  protected readonly loading = signal(!this.invalidId);
  protected readonly loadError = signal(false);

  protected readonly draft = signal<SuspensionDraft>({ reason: '', endDate: '' });
  protected readonly suspensionForm = form(this.draft, (path) => {
    required(path.reason, { message: 'Veuillez renseigner le motif de la suspension.' });
  });

  protected readonly endDateError = signal('');
  protected readonly submitError = signal('');

  constructor() {
    if (!this.invalidId) {
      this.accountApi.getUserById(this.userId).subscribe({
        next: (user) => {
          this.user.set(user);
          this.loading.set(false);
        },
        error: () => {
          this.loadError.set(true);
          this.loading.set(false);
        },
      });
    }
  }

  protected async save(event: Event): Promise<void> {
    event.preventDefault();
    this.endDateError.set('');
    this.submitError.set('');

    await submit(this.suspensionForm, async (field) => {
      const { reason, endDate } = field().value();

      // Le login ne bloque que tant que la date du jour est avant la date de fin : une date
      // d'aujourd'hui ou passée n'aurait aucun effet (le back ne le contrôle pas).
      if (endDate && endDate <= toIsoDate(new Date())) {
        this.endDateError.set('La date de fin doit être postérieure à aujourd’hui.');
        return undefined;
      }

      try {
        await firstValueFrom(
          this.accountApi.suspendUser(this.userId, {
            reason: reason.trim(),
            endDate: endDate || null,
          }),
        );
        await this.router.navigate(['/admin/comptes']);
      } catch (error) {
        this.submitError.set(suspensionErrorMessage(error));
      }
      return undefined;
    });
  }

  protected async cancel(): Promise<void> {
    await this.router.navigate(['/admin/comptes']);
  }
}
