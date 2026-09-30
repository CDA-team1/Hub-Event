import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AccountApi } from '../../../core/accounts/account-api';
import { CreateUserRequest } from '../../../core/accounts/create-user-request';
import { SignupForm } from '../signup-form/signup-form';

@Component({
  selector: 'app-signup-page',
  imports: [SignupForm],
  templateUrl: './signup-page.html',
  styleUrl: './signup-page.css',
})
export class SignupPage {
  private readonly accountApi = inject(AccountApi);
  private readonly router = inject(Router);

  readonly success = signal(false);
  readonly emailError = signal<string | null>(null);

  signup(request: CreateUserRequest): void {
    this.emailError.set(null);

    this.accountApi.signup(request).subscribe({
      next: () => {
        this.success.set(true);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 400 && typeof error.error === 'string') {
          this.emailError.set(error.error);
        }
      },
    });
  }

  cancel(): void {
    void this.router.navigate(['/']);
  }
}
