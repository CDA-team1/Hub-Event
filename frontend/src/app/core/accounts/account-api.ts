import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

import { AccountDto, UserProfileDto } from '../../domain/account.model';
import { UpdateUserRequest } from '../../domain/update-user-request';
import { ConfirmAccountCreationRequest } from '../../domain/confirm-account-creation-request';
import { API_URL } from '../http/api-url';

@Service()
export class AccountApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = inject(API_URL);

  activateAccount(token: string): Observable<void> {
    const params = new HttpParams().set('token', token);

    return this.http.get<void>(`${this.apiUrl}/auth/activate`, { params });
  }

  confirmAccountCreation(
    token: string,
    request: ConfirmAccountCreationRequest,
  ): Observable<AccountDto> {
    const params = new HttpParams().set('token', token);

    return this.http.post<AccountDto>(`${this.apiUrl}/auth/confirm-account-creation`, request, {
      params,
    });
  }

  getOwnAccount(): Observable<UserProfileDto> {
    return this.http.get<UserProfileDto>(`${this.apiUrl}/users/me`);
  }

  updateOwnAccount(request: UpdateUserRequest): Observable<AccountDto> {
    return this.http.put<AccountDto>(`${this.apiUrl}/users/me`, request);
  }
}
