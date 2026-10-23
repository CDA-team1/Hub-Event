import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { CommentDto } from '../../domain/event.model';
import { API_URL } from '../http/api-url';

@Service()
export class CommentApi {
  private readonly apiUrl = inject(API_URL);
  private readonly http = inject(HttpClient);

  async create(eventId: number, content: string): Promise<CommentDto> {
    return firstValueFrom(
      this.http.post<CommentDto>(`${this.apiUrl}/comments`, {
        eventId,
        content,
      }),
    );
  }
}
