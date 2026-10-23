import { DatePipe } from '@angular/common';
import { Component, input } from '@angular/core';

import { CommentDto } from '../../../../domain/event.model';
import { EmptyState } from '../../../../shared/ui/empty-state/empty-state';

@Component({
  selector: 'app-comment-list',
  imports: [DatePipe, EmptyState],
  templateUrl: './comment-list.html',
  styleUrl: './comment-list.css',
})
export class CommentList {
  readonly comments = input.required<CommentDto[]>();
}
