import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from '../http/api-url';
import { CreateUserRequest } from './create-user-request';

@Service()
export class AccountApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_URL);

  signup(request: CreateUserRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/signup`, request);
  }

  activateAccount(token: string): Observable<void> {
    const params = new HttpParams().set('token', token);

    return this.http.get<void>(`${this.apiUrl}/auth/activate`, { params });
  }
}
