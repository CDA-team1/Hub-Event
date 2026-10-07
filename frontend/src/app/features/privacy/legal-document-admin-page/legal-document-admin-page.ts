import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { LegalDocumentApi } from '../../../core/privacy/legal-document-api';
import { LegalDocumentType } from '../../../domain/legal-document.model';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';

@Component({
  selector: 'app-legal-document-admin-page',
  imports: [ReactiveFormsModule, ErrorState, LoadingState],
  templateUrl: './legal-document-admin-page.html',
  styleUrl: './legal-document-admin-page.css',
})
export class LegalDocumentAdminPage {
  private readonly legalDocumentApi = inject(LegalDocumentApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly type = this.readType();

  readonly title =
    this.type === 'RGPD'
      ? 'Modifier la politique RGPD'
      : 'Modifier les Conditions Générales d’Utilisation';

  readonly content = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required],
  });

  readonly loading = signal(true);
  readonly saving = signal(false);

  readonly loadErrorMessage = signal<string | null>(null);
  readonly saveErrorMessage = signal<string | null>(null);

  constructor() {
    this.loadDocument();
  }

  save(): void {
    if (this.saving()) {
      return;
    }

    if (this.content.invalid || this.content.value.trim().length === 0) {
      this.content.setErrors({ required: true });
      this.content.markAsTouched();
      return;
    }

    this.saving.set(true);
    this.saveErrorMessage.set(null);

    this.legalDocumentApi.upsert(this.type, this.content.value).subscribe({
      next: (document) => {
        this.content.setValue(document.content);
        this.saving.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);

        if (error.status === 400 && typeof error.error === 'string') {
          this.saveErrorMessage.set(error.error);
          return;
        }

        this.saveErrorMessage.set("Impossible d'enregistrer le document.");
      },
    });
  }

  cancel(): void {
    void this.router.navigate(['/']);
  }

  retry(): void {
    this.loadDocument();
  }

  private loadDocument(): void {
    this.loading.set(true);
    this.loadErrorMessage.set(null);
    this.saveErrorMessage.set(null);

    this.legalDocumentApi.getByType(this.type).subscribe({
      next: (document) => {
        this.content.setValue(document.content);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);

        if (error.status === 404) {
          this.content.setValue('');
          return;
        }

        this.loadErrorMessage.set(
          this.type === 'RGPD'
            ? 'Impossible de charger la politique RGPD.'
            : 'Impossible de charger les Conditions Générales d’Utilisation.',
        );
      },
    });
  }

  private readType(): LegalDocumentType {
    const type: unknown = this.route.snapshot.data['legalDocumentType'];

    if (type !== 'RGPD' && type !== 'CGU') {
      throw new Error('Type de document légal invalide.');
    }

    return type;
  }
}
