import { HttpClient, httpResource } from '@angular/common/http';
import { Service, Signal, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { RegistrationDto } from '../../domain/event.model';
import { API_URL } from '../http/api-url';

@Service()
export class RegistrationApi {
  private readonly apiUrl = inject(API_URL);
  private readonly http = inject(HttpClient);

  /** Inscrit l'utilisateur connecté à l'évènement (liste d'attente si l'évènement est complet). */
  async register(eventId: number): Promise<RegistrationDto> {
    return firstValueFrom(
      this.http.post<RegistrationDto>(`${this.apiUrl}/events/${eventId}/registrations`, null),
    );
  }

  /** Inscriptions à un évènement, réservé à son organisateur (REG-04). */
  forEvent(eventId: Signal<number>) {
    return httpResource<RegistrationDto[]>(
      () => `${this.apiUrl}/events/${eventId()}/registrations`,
      { defaultValue: [] },
    );
  }

  /** Désinscrit l'utilisateur connecté de l'évènement. */
  async unregister(eventId: number): Promise<void> {
    await firstValueFrom(
      this.http.delete<void>(`${this.apiUrl}/events/${eventId}/registrations/me`),
    );
  }

  /** Désinscrit un membre à la demande de l'organisateur, avec motif obligatoire (REG-04, CU22). */
  async cancelByOrganizer(eventId: number, userId: number, reason: string): Promise<void> {
    await firstValueFrom(
      this.http.delete<void>(`${this.apiUrl}/events/${eventId}/registrations/${userId}`, {
        body: { reason },
      }),
    );
  }
}
