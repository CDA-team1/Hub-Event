import { HttpClient, httpResource } from '@angular/common/http';
import { inject, Service, Signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { AdminAnonymizationDto, AnonymizationDto } from '../../domain/anonymization.model';
import { PageDto } from '../../domain/page.model';
import { API_URL } from '../http/api-url';

export interface AnonymizationListParams {
  readonly page: number;
  readonly size: number;
}

@Service()
export class AnonymizationApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_URL);

  async createRequest(): Promise<AnonymizationDto> {
    return firstValueFrom(
      this.http.post<AnonymizationDto>(`${this.apiUrl}/anonymization-requests`, null),
    );
  }

  async validate(id: number): Promise<AnonymizationDto> {
    return firstValueFrom(
      this.http.post<AnonymizationDto>(`${this.apiUrl}/admin/anonymization/${id}/validate`, null),
    );
  }

  listPending(params: Signal<AnonymizationListParams>) {
    return httpResource<PageDto<AdminAnonymizationDto>>(
      () => ({
        url: `${this.apiUrl}/admin/anonymization`,
        params: {
          page: String(params().page),
          size: String(params().size),
        },
      }),
      {
        defaultValue: {
          content: [],
          page: 0,
          size: params().size,
          totalElements: 0,
          totalPages: 0,
          first: true,
          last: true,
        },
      },
    );
  }
}
