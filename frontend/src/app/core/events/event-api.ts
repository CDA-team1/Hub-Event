import {inject, Service, Signal} from '@angular/core';
import {httpResource, HttpClient} from '@angular/common/http';
import {
  CommentDto,
  CreateEventRequest,
  EventCardDto,
  EventDetailResponse,
  EventDto,
  EventListDto,
  EventSearchCriteria,
  ImageDto,
  OrganizerEventDto,
  UpdateEventRequest
} from '../../domain/event.model';
import {firstValueFrom} from 'rxjs';
import { API_URL } from '../http/api-url';
import { criteriaToParams } from '../../domain/event-search';

export function isValidEventId(id: number): boolean {
  return Number.isInteger(id) && id > 0;
}

@Service()
export class EventApi {
  private readonly apiUrl = inject(API_URL);
  private readonly http = inject(HttpClient);

  publicEvents() {
    return httpResource<EventListDto>(() => `${this.apiUrl}/events`, {
      defaultValue: {cultureEvents: [], leisureEvents: [], sportEvents: [], pastEvents: []},
    });
  }

  /** Les évènements créés par l'organisateur connecté, tous statuts confondus (EVT-05). */
  mine() {
    return httpResource<OrganizerEventDto[]>(() => `${this.apiUrl}/events/mine`, {
      defaultValue: [],
    });
  }

  eventDetail(id: Signal<number>) {
    return httpResource<EventDetailResponse>(() =>
      isValidEventId(id()) ? `${this.apiUrl}/events/${id()}` : undefined,
    );
  }

  search(criteria: Signal<EventSearchCriteria>) {
    return httpResource<EventCardDto[]>(
      () => ({ url: `${this.apiUrl}/events/search`, params: criteriaToParams(criteria()) }),
      { defaultValue: [] },
    );
  }

  comment(id: Signal<number>) {
    return httpResource<CommentDto[]>(
      () => (isValidEventId(id()) ? `${this.apiUrl}/events/${id()}/comments` : undefined),
      { defaultValue: [] },
    );
  }

  async create(request: CreateEventRequest): Promise<EventDto> {
    return firstValueFrom(this.http.post<EventDto>(`${this.apiUrl}/events`, request));
  }

  async update(id: number, request: UpdateEventRequest): Promise<EventDto> {
    return firstValueFrom(this.http.put<EventDto>(`${this.apiUrl}/events/${id}`, request));
  }

  async publish(id: number): Promise<EventDto> {
    return firstValueFrom(this.http.post<EventDto>(`${this.apiUrl}/events/${id}/publish`, null));
  }

  async finish(id: number): Promise<EventDto> {
    return firstValueFrom(this.http.post<EventDto>(`${this.apiUrl}/events/${id}/status`, null));
  }

  async cancel(id: number): Promise<EventDto> {
    return firstValueFrom(this.http.post<EventDto>(`${this.apiUrl}/events/${id}/cancel`, null));
  }

  async delete(id: number): Promise<void> {
    await firstValueFrom(this.http.delete<void>(`${this.apiUrl}/events/${id}`));
  }

  async addImages(eventId: number, files: File[]): Promise<ImageDto[]> {
    const formData = new FormData();
    for (const file of files) {
      formData.append('files', file);
    }
    return firstValueFrom(
      this.http.post<ImageDto[]>(`${this.apiUrl}/events/${eventId}/images`, formData),
    );
  }

  async removeImage(eventId: number, imageId: number): Promise<void> {
    await firstValueFrom(
      this.http.delete<void>(`${this.apiUrl}/events/${eventId}/images/${imageId}`),
    );
  }

  async getPdf(id: number): Promise<Blob> {
    return firstValueFrom(
      this.http.get(`${this.apiUrl}/events/${id}/pdf`, { responseType: 'blob' }),
    );
  }
}
