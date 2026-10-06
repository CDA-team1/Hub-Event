import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { AnonymizationDto } from '../../domain/anonymization.model';
import { API_URL } from '../http/api-url';

@Service()
export class AnonymizationApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_URL);

  async createRequest(): Promise<AnonymizationDto> {
    return firstValueFrom(
      this.http.post<AnonymizationDto>(`${this.apiUrl}/anonymization-requests`, null),
    );
  }
}
