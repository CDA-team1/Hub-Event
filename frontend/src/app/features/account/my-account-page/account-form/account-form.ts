import { Component, effect, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { UserProfileDto } from '../../../../domain/account.model';
import { UpdateUserRequest } from '../../../../domain/update-user-request';

@Component({
  selector: 'app-account-form',
  imports: [ReactiveFormsModule],
  templateUrl: './account-form.html',
  styleUrl: './account-form.css',
})
export class AccountForm {
  readonly profile = input.required<UserProfileDto>();
  readonly emailError = input<string | null>(null);

  readonly submitted = output<UpdateUserRequest>();
  readonly cancelled = output<void>();

  readonly form = new FormGroup({
    lastName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    firstName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    postalAddress: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    phone: new FormControl<string | null>(null),
    password: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.minLength(12),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/),
      ],
    }),
  });

  private readonly syncProfile = effect(() => {
    const profile = this.profile();

    this.form.patchValue({
      lastName: profile.lastName,
      firstName: profile.firstName,
      postalAddress: profile.postalAddress,
      email: profile.email,
      phone: profile.phone,
      password: '',
    });
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();

    this.submitted.emit({
      lastName: value.lastName,
      firstName: value.firstName,
      postalAddress: value.postalAddress,
      email: value.email,
      phone: value.phone?.trim() ? value.phone.trim() : null,
      password: value.password || null,
    });
  }

  cancel(): void {
    this.cancelled.emit();
  }
}
