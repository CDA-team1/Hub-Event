import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Auth } from '../../../core/auth/auth';
import { CreateUserRequest } from '../../../domain/create-user-request';
import { SignupForm } from './signup-form/signup-form';

@Component({
  selector: 'app-signup-page',
  imports: [SignupForm],
  templateUrl: './signup-page.html',
  styleUrl: './signup-page.css',
})
export class SignupPage {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  readonly success = signal(false);
  readonly emailError = signal<string | null>(null);

  async signup(request: CreateUserRequest): Promise<void> {
    this.emailError.set(null);

    try {
      await this.auth.signup(request);
      this.success.set(true);
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 400 && typeof error.error === 'string') {
        this.emailError.set(error.error);
      }
    }
  }

  cancel(): void {
    void this.router.navigate(['/']);
  }
}
