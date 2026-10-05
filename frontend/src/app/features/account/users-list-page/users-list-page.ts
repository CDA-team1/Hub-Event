import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AccountApi } from '../../../core/accounts/account-api';
import { AccountDto } from '../../../domain/account.model';
import { Role } from '../../../domain/role';
import { AccountCard } from '../../../shared/ui/account-card/account-card';
import { EmptyState } from '../../../shared/ui/empty-state/empty-state';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { Pagination } from '../../../shared/ui/pagination/pagination';

function isRole(value: string | null): value is Role {
  return value === 'MEMBER' || value === 'ORGANIZER' || value === 'ADMIN';
}

@Component({
  selector: 'app-users-list-page',
  imports: [AccountCard, EmptyState, ErrorState, LoadingState, Pagination, RouterLink],
  templateUrl: './users-list-page.html',
  styleUrl: './users-list-page.css',
})
export class UsersListPage {
  private readonly accountApi = inject(AccountApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly users = signal<AccountDto[]>([]);
  protected readonly selectedRole = signal<Role | null>(null);
  protected readonly page = signal(0);
  protected readonly pageSize = signal(20);
  protected readonly totalElements = signal(0);

  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');

  constructor() {
    this.route.queryParamMap.subscribe((params) => {
      const role = params.get('role');
      const page = Number(params.get('page') ?? '0');

      this.selectedRole.set(isRole(role) ? role : null);
      this.page.set(Number.isInteger(page) && page >= 0 ? page : 0);

      this.loadUsers();
    });
  }

  protected changeRole(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    const role = isRole(value) ? value : null;

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        role,
        page: 0,
      },
      queryParamsHandling: 'merge',
    });
  }

  protected changePage(page: number): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page },
      queryParamsHandling: 'merge',
    });
  }

  protected reload(): void {
    this.loadUsers();
  }

  private loadUsers(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.accountApi.getUsers(this.selectedRole(), this.page()).subscribe({
      next: (result) => {
        this.users.set(result.content);
        this.page.set(result.page);
        this.pageSize.set(result.size);
        this.totalElements.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger la liste des comptes.');
        this.loading.set(false);
      },
    });
  }
}
