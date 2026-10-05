import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

import { ClubCardDto } from '../../domain/club.model';
import { PageDto } from '../../domain/page.model';
import { API_URL } from '../http/api-url';

@Service()
export class ClubApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_URL);

  getClubs(page: number, size: number): Observable<PageDto<ClubCardDto>> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http.get<PageDto<ClubCardDto>>(`${this.apiUrl}/clubs`, { params });
  }
}
