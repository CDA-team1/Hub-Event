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
  UpdateEventRequest
} from '../../domain/event.model';
import {firstValueFrom} from 'rxjs';
import {API_URL} from '../http/api-url';

function toSearchParams(criteria: EventSearchCriteria): Record<string, string> {
  const params: Record<string, string> = {};
  if (criteria.category) params['category'] = criteria.category;
  if (criteria.minPrice !== undefined) params['minPrice'] = String(criteria.minPrice);
  if (criteria.maxPrice !== undefined) params['maxPrice'] = String(criteria.maxPrice);
  if (criteria.location) params['location'] = criteria.location;
  if (criteria.startDate) params['startDate'] = criteria.startDate;
  if (criteria.endDate) params['endDate'] = criteria.endDate;
  if (criteria.keywords) params['keywords'] = criteria.keywords;
  return params;
}

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

  eventDetail(id: Signal<number>) {
    return httpResource<EventDetailResponse>(() =>
      isValidEventId(id()) ? `${this.apiUrl}/events/${id()}` : undefined,
    );
  }

  search(criteria: Signal<EventSearchCriteria>) {
    return httpResource<EventCardDto[]>(
      () => ({ url: `${this.apiUrl}/events/search`, params: toSearchParams(criteria()) }),
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

  async unregister(eventId: number): Promise<void> {
    await firstValueFrom(
      this.http.delete<void>(`${this.apiUrl}/events/${eventId}/registrations/me`),
    );
  }
}
