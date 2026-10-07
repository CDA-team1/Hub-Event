import { HttpClient, httpResource } from '@angular/common/http';
import { Service, Signal, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { EventCardDto } from '../../domain/event.model';
import { API_URL } from '../http/api-url';

export interface DateRange {
  readonly from: string;
  readonly to: string;
}

@Service()
export class CalendarApi {
  private readonly apiUrl = inject(API_URL);
  private readonly http = inject(HttpClient);

  /** Calendrier de la période au format Excel (.xlsx), à télécharger (CAL-02). */
  async exportExcel(range: DateRange): Promise<Blob> {
    return firstValueFrom(
      this.http.get(`${this.apiUrl}/calendar/export`, {
        params: { from: range.from, to: range.to },
        responseType: 'blob',
      }),
    );
  }

  /** Évènements auxquels l'utilisateur connecté est inscrit, sur la période donnée. */
  myCalendar(range: Signal<DateRange>) {
    return httpResource<EventCardDto[]>(
      () => ({ url: `${this.apiUrl}/calendar`, params: { from: range().from, to: range().to } }),
      { defaultValue: [] },
    );
  }
}
