import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { map } from 'rxjs';

import { Auth } from '../../../core/auth/auth';
import { CommentApi } from '../../../core/comments/comment-api';
import { EventApi, isValidEventId } from '../../../core/events/event-api';
import { RegistrationApi } from '../../../core/registrations/registration-api';
import { CommentForm } from '../../../shared/ui/comment-form/comment-form';
import { CommentList } from '../../../shared/ui/comment-list/comment-list';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { NotFoundPage } from '../../not-found/not-found-page/not-found-page';
import { ImageGallery } from '../image-gallery/image-gallery';
import { RegistrationPanel } from '../registration-panel/registration-panel';
import { Confirmation } from '../../../core/dialog/confirmation';
import { downloadBlob } from '../../../core/files/download-blob';

/** Message à afficher pour une inscription refusée, tel que renvoyé par le back. */
function registrationErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse && typeof error.error === 'string') {
    return error.error;
  }

  return "L'inscription a échoué, réessayez.";
}

function commentErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse && typeof error.error === 'string') {
    return error.error;
  }

  return 'La publication du commentaire a échoué, réessayez.';
}

@Component({
  selector: 'app-event-detail-page',
  imports: [
    DatePipe,
    RouterLink,
    CommentForm,
    CommentList,
    ErrorState,
    ImageGallery,
    LoadingState,
    NotFoundPage,
    RegistrationPanel,
  ],
  styleUrl: './event-detail-page.css',
  templateUrl: './event-detail-page.html',
})
export class EventDetailPage {
  private readonly route = inject(ActivatedRoute);
  private readonly eventApi = inject(EventApi);
  private readonly commentApi = inject(CommentApi);
  private readonly confirmation = inject(Confirmation);
  private readonly registrationApi = inject(RegistrationApi);
  private readonly auth = inject(Auth);

  private readonly commentForm = viewChild(CommentForm);

  private readonly id = toSignal(
    this.route.paramMap.pipe(map((params) => Number(params.get('id')))),
    { initialValue: Number(this.route.snapshot.paramMap.get('id')) },
  );

  protected readonly detail = this.eventApi.eventDetail(this.id);
  protected readonly comments = this.eventApi.comment(this.id);

  protected readonly isConnected = this.auth.isAuthenticated;

  protected readonly registering = signal(false);
  protected readonly unregistering = signal(false);
  protected readonly registrationError = signal('');
  protected readonly pdfDownloading = signal(false);
  protected readonly pdfError = signal('');

  protected readonly commentSubmitting = signal(false);
  protected readonly commentError = signal('');

  protected readonly event = computed(() =>
    this.detail.hasValue() ? this.detail.value() : undefined,
  );

  protected readonly isNotFound = computed(() => {
    const error = this.detail.error() as { status?: number } | undefined;
    return !isValidEventId(this.id()) || error?.status === 404;
  });

  /** Bouton bloqué pendant l'appel d'inscription et pendant le rechargement qui suit. */
  protected readonly registrationPending = computed(
    () => this.registering() || this.unregistering() || this.detail.isLoading(),
  );

  protected async register(): Promise<void> {
    this.registrationError.set('');
    this.registering.set(true);

    try {
      await this.registrationApi.register(this.id());
      this.detail.reload();
    } catch (error) {
      this.registrationError.set(registrationErrorMessage(error));
    } finally {
      this.registering.set(false);
    }
  }

  protected async unregister(): Promise<void> {
    const current = this.event();

    if (!current) {
      return;
    }

    this.registrationError.set('');

    const confirmed = await this.confirmation.confirm(
      `Se désinscrire de « ${current.title} » ?`,
      'Se désinscrire',
      'Annuler',
    );

    if (!confirmed) {
      return;
    }

    this.unregistering.set(true);

    try {
      await this.registrationApi.unregister(this.id());
      this.detail.reload();
    } catch (error) {
      if (error instanceof HttpErrorResponse && typeof error.error === 'string') {
        this.registrationError.set(error.error);
      } else {
        this.registrationError.set('La désinscription a échoué, réessayez.');
      }
    } finally {
      this.unregistering.set(false);
    }
  }

  protected async downloadPdf(): Promise<void> {
    this.pdfError.set('');
    this.pdfDownloading.set(true);

    try {
      downloadBlob(await this.eventApi.getPdf(this.id()), `evenement-${this.id()}.pdf`);
    } catch {
      this.pdfError.set('Le téléchargement de la fiche PDF a échoué, réessayez.');
    } finally {
      this.pdfDownloading.set(false);
    }
  }

  protected async addComment(content: string): Promise<void> {
    if (this.commentSubmitting()) {
      return;
    }

    this.commentError.set('');
    this.commentSubmitting.set(true);

    try {
      await this.commentApi.create(this.id(), content);

      this.commentForm()?.reset();
      this.comments.reload();
    } catch (error) {
      this.commentError.set(commentErrorMessage(error));
    } finally {
      this.commentSubmitting.set(false);
    }
  }
}
