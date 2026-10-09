import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';

import { LegalDocumentApi } from '../../../core/privacy/legal-document-api';
import { LegalDocumentDto } from '../../../domain/legal-document.model';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';

@Component({
  selector: 'app-rgpd-page',
  imports: [LoadingState, ErrorState, EmptyState],
  templateUrl: './rgpd-page.html',
  styleUrl: './rgpd-page.css',
})
export class RgpdPage {
  private readonly legalDocumentApi = inject(LegalDocumentApi);

  readonly document = signal<LegalDocumentDto | null>(null);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    this.loadRgpd();
  }

  retry(): void {
    this.loadRgpd();
  }

  private loadRgpd(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.legalDocumentApi.getByType('RGPD').subscribe({
      next: (document) => {
        this.document.set(document);
        this.loading.set(false);
      },
      error: (_error: HttpErrorResponse) => {
        this.loading.set(false);
        this.errorMessage.set('Impossible de charger la politique RGPD.');
      },
    });
  }
}
