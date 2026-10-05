import { Service, Signal, inject } from '@angular/core';
import { HttpClient, HttpParams, httpResource } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ClubDto, ClubFormRequest } from '../../domain/club.model';
import { Observable } from 'rxjs';

import { PageDto } from '../../domain/page.model';
import { API_URL } from '../http/api-url';

export interface ClubListParams {
  readonly page: number;
  readonly size: number;
}

@Service()
export class ClubApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_URL);

  /** Clubs actifs, paginés (le back exclut déjà les clubs désaffiliés). */
  list(params: Signal<ClubListParams>) {
    return httpResource<PageDto<ClubDto>>(
      () => ({
        url: `${this.apiUrl}/clubs`,
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

  get(id: Signal<number>) {
    return httpResource<ClubDto>(() => `${this.apiUrl}/clubs/${id()}`);
  }

  async create(request: ClubFormRequest): Promise<ClubDto> {
    return firstValueFrom(this.http.post<ClubDto>(`${this.apiUrl}/clubs`, request));
  }

  async update(id: number, request: ClubFormRequest): Promise<ClubDto> {
    return firstValueFrom(this.http.put<ClubDto>(`${this.apiUrl}/clubs/${id}`, request));
  }

  /** Met fin à l'affiliation du club (soft-delete back : `validityEndDate`). */
  async delete(id: number): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`${this.apiUrl}/clubs/${id}`));
  }

  getClubs(page: number, size: number): Observable<PageDto<ClubDto>> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http.get<PageDto<ClubDto>>(`${this.apiUrl}/clubs`, { params });
  }
}
