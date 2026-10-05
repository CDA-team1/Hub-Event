import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

import { PageDto } from '../../domain/page.model';
import { Role } from '../../domain/role';
import { AccountDto, AdminUserDto, UserProfileDto } from '../../domain/account.model';
import { UpdateUserRequest } from '../../domain/update-user-request';
import { ConfirmAccountCreationRequest } from '../../domain/confirm-account-creation-request';
import { AdminUserRequest } from '../../domain/admin-user-request';
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

  getUsers(role: Role | null, page: number): Observable<PageDto<AccountDto>> {
    let params = new HttpParams().set('page', page);

    if (role) {
      params = params.set('role', role);
    }

    return this.http.get<PageDto<AccountDto>>(`${this.apiUrl}/admin/users`, { params });
  }

  getUserById(id: number): Observable<AdminUserDto> {
    return this.http.get<AdminUserDto>(`${this.apiUrl}/admin/users/${id}`);
  }

  createUser(request: AdminUserRequest): Observable<AccountDto> {
    return this.http.post<AccountDto>(`${this.apiUrl}/admin/users`, request);
  }

  updateUser(id: number, request: AdminUserRequest): Observable<AccountDto> {
    return this.http.put<AccountDto>(`${this.apiUrl}/admin/users/${id}`, request);
  }
}
