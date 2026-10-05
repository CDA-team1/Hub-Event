import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AccountApi } from '../../../core/accounts/account-api';
import { ClubApi } from '../../../core/clubs/club-api';
import { AdminUserDto } from '../../../domain/account.model';
import { AdminUserRequest } from '../../../domain/admin-user-request';
import { ClubCardDto } from '../../../domain/club.model';
import { Role } from '../../../domain/role';
import { ErrorState } from '../../../shared/ui/error-state/error-state';
import { LoadingState } from '../../../shared/ui/loading-state/loading-state';
import { UserForm } from '../user-form/user-form';

const CLUB_PAGE_SIZE = 20;

function isRole(value: string): value is Role {
  return value === 'MEMBER' || value === 'ORGANIZER' || value === 'ADMIN';
}

@Component({
  selector: 'app-user-form-page',
  imports: [UserForm, ErrorState, LoadingState],
  templateUrl: './user-form-page.html',
  styleUrl: './user-form-page.css',
})
export class UserFormPage {
  private readonly accountApi = inject(AccountApi);
  private readonly clubApi = inject(ClubApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly user = signal<AdminUserDto | null>(null);
  readonly clubs = signal<ClubCardDto[]>([]);
  readonly selectedRole = signal<Role | null>(null);

  readonly editing = signal(false);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly invalidId = signal(false);

  readonly loadErrorMessage = signal<string | null>(null);
  readonly saveErrorMessage = signal<string | null>(null);
  readonly emailError = signal<string | null>(null);

  private userId: number | null = null;

  constructor() {
    const rawId = this.route.snapshot.paramMap.get('id');

    if (rawId === null) {
      this.loadClubs();
      return;
    }

    const id = Number(rawId);

    if (!Number.isInteger(id) || id <= 0) {
      this.invalidId.set(true);
      this.loadErrorMessage.set('Identifiant de compte invalide.');
      this.loading.set(false);
      return;
    }

    this.userId = id;
    this.editing.set(true);

    this.loadClubs();
  }

  changeRole(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;

    this.selectedRole.set(isRole(value) ? value : null);
  }

  save(request: AdminUserRequest): void {
    if (this.saving()) {
      return;
    }

    this.saving.set(true);
    this.saveErrorMessage.set(null);
    this.emailError.set(null);

    const request$ =
      this.userId === null
        ? this.accountApi.createUser(request)
        : this.accountApi.updateUser(this.userId, request);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        void this.router.navigate(['/admin/comptes']);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);

        if (error.status === 400 && typeof error.error === 'string') {
          if (error.error.toLowerCase().includes('email')) {
            this.emailError.set(error.error);
            return;
          }

          this.saveErrorMessage.set(error.error);
          return;
        }

        this.saveErrorMessage.set(
          this.editing()
            ? 'Une erreur est survenue lors de la modification du compte.'
            : 'Une erreur est survenue lors de la création du compte.',
        );
      },
    });
  }

  cancel(): void {
    void this.router.navigate(['/admin/comptes']);
  }

  reload(): void {
    this.loadClubs();
  }

  private loadClubs(): void {
    this.loading.set(true);
    this.loadErrorMessage.set(null);
    this.clubs.set([]);

    if (this.editing()) {
      this.user.set(null);
    }

    this.loadClubPage(0, []);
  }

  private loadClubPage(page: number, accumulatedClubs: ClubCardDto[]): void {
    this.clubApi.getClubs(page, CLUB_PAGE_SIZE).subscribe({
      next: (result) => {
        const allClubs = [...accumulatedClubs, ...result.content];

        if (result.page + 1 < result.totalPages) {
          this.loadClubPage(result.page + 1, allClubs);
          return;
        }

        const activeClubs = allClubs.filter((club) => club.validityEndDate === null);

        this.clubs.set(activeClubs);

        if (this.userId === null) {
          this.loading.set(false);
          return;
        }

        this.loadUser(this.userId);
      },
      error: () => {
        this.loadErrorMessage.set('Impossible de charger la liste des clubs.');
        this.loading.set(false);
      },
    });
  }

  private loadUser(id: number): void {
    this.accountApi.getUserById(id).subscribe({
      next: (user) => {
        const activeClubIds = new Set(this.clubs().map((club) => club.id));

        const normalizedUser: AdminUserDto = {
          ...user,
          clubs: user.clubs.filter((club) => activeClubIds.has(club.id)),
        };

        this.user.set(normalizedUser);
        this.selectedRole.set(user.role);
        this.loading.set(false);
      },
      error: () => {
        this.loadErrorMessage.set('Impossible de charger les informations du compte.');
        this.loading.set(false);
      },
    });
  }
}
