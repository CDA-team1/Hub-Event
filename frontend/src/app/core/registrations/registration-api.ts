import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { API_URL } from '../http/api-url';

@Service()
export class RegistrationApi {
  private readonly apiUrl = inject(API_URL);
  private readonly http = inject(HttpClient);

  /** Désinscrit l'utilisateur connecté de l'évènement. */
  async unregister(eventId: number): Promise<void> {
    await firstValueFrom(
      this.http.delete<void>(`${this.apiUrl}/events/${eventId}/registrations/me`),
    );
  }
}
