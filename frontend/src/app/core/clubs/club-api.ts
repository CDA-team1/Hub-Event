import { httpResource } from '@angular/common/http';
import { Service, Signal, inject } from '@angular/core';
import { ClubDto } from '../../domain/club.model';
import { PageDto } from '../../domain/page';
import { API_URL } from '../http/api-url';

export interface ClubListParams {
  readonly page: number;
  readonly size: number;
}

@Service()
export class ClubApi {
  private readonly apiUrl = inject(API_URL);

  /** Clubs actifs, paginés (le back exclut déjà les clubs désaffiliés). */
  list(params: Signal<ClubListParams>) {
    return httpResource<PageDto<ClubDto>>(
      () => ({
        url: `${this.apiUrl}/clubs`,
        params: { page: String(params().page), size: String(params().size) },
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
