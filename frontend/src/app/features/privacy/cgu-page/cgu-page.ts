import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';

import { LegalDocumentApi } from '../../../core/privacy/legal-document-api';
import { LegalDocumentDto } from '../../../domain/legal-document.model';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';

@Component({
  selector: 'app-cgu-page',
  imports: [LoadingState, ErrorState],
  templateUrl: './cgu-page.html',
  styleUrl: './cgu-page.css',
})
export class CguPage {
  private readonly legalDocumentApi = inject(LegalDocumentApi);

  readonly document = signal<LegalDocumentDto | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    this.loadCgu();
  }

  retry(): void {
    this.loadCgu();
  }

  private loadCgu(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.legalDocumentApi.getByType('CGU').subscribe({
      next: (document) => {
        this.document.set(document);
        this.loading.set(false);
      },
      error: (_error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger les Conditions Générales d’Utilisation.');
      },
    });
  }
}
