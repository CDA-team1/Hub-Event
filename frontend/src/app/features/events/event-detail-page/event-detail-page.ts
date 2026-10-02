import { DatePipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs';

import { EventApi, isValidEventId } from '../../../core/events/event-api';
import { CommentList } from '../../../shared/ui/comment-list/comment-list';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { NotFoundPage } from '../../not-found/not-found-page/not-found-page';
import { ImageGallery } from '../image-gallery/image-gallery';

@Component({
  selector: 'app-event-detail-page',
  imports: [DatePipe, CommentList, ErrorState, ImageGallery, LoadingState, NotFoundPage],
  styleUrl: './event-detail-page.css',
  templateUrl: './event-detail-page.html',
})
export class EventDetailPage {
  private readonly route = inject(ActivatedRoute);
  private readonly eventApi = inject(EventApi);

  private readonly id = toSignal(
    this.route.paramMap.pipe(map((params) => Number(params.get('id')))),
    { initialValue: Number(this.route.snapshot.paramMap.get('id')) },
  );

  protected readonly detail = this.eventApi.eventDetail(this.id);
  protected readonly comments = this.eventApi.comment(this.id);

  protected readonly event = computed(() =>
    this.detail.hasValue() ? this.detail.value() : undefined,
  );

  protected readonly isNotFound = computed(() => {
    const error = this.detail.error() as { status?: number } | undefined;
    return !isValidEventId(this.id()) || error?.status === 404;
  });
}
