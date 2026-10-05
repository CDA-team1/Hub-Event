import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubApi } from '../../../core/clubs/club-api';
import { ClubFormRequest } from '../../../domain/club.model';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { ClubForm } from './club-form/club-form';

function submitErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse && typeof error.error === 'string') {
    return error.error;
  }
  return "L'enregistrement a échoué.";
}

@Component({
  selector: 'app-club-form-page',
  imports: [ClubForm, ErrorState, LoadingState],
  templateUrl: './club-form-page.html',
  styleUrl: './club-form-page.css',
})
export class ClubFormPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly clubApi = inject(ClubApi);

  private readonly idParam = this.route.snapshot.paramMap.get('id');
  protected readonly isEditMode = this.idParam !== null;
  private readonly clubId = signal(Number(this.idParam ?? 0));

  private readonly club = this.isEditMode ? this.clubApi.get(this.clubId) : undefined;

  protected readonly isLoading = computed(() => this.club?.isLoading() ?? false);
  protected readonly error = computed(() => this.club?.error());
  protected readonly ready = computed(() => !this.isEditMode || (this.club?.hasValue() ?? false));

  protected readonly initialValue = computed<ClubFormRequest | undefined>(() => {
    if (!this.club?.hasValue()) {
      return undefined;
    }
    const club = this.club.value();
    return {
      name: club.name,
      category: club.category,
      postalAddress: club.postalAddress,
      email: club.email,
      phone: club.phone,
    };
  });

  protected readonly submitError = signal('');

  protected async onSubmitted(request: ClubFormRequest): Promise<void> {
    this.submitError.set('');
    try {
      if (this.isEditMode) {
        await this.clubApi.update(this.clubId(), request);
      } else {
        await this.clubApi.create(request);
      }
      await this.router.navigate(['/clubs']);
    } catch (error) {
      this.submitError.set(submitErrorMessage(error));
    }
  }

  protected async onCancelled(): Promise<void> {
    await this.router.navigate(['/clubs']);
  }
}
