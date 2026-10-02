import { Component, input, output } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';

import { CreateUserRequest } from '../../../core/accounts/create-user-request';

function passwordsMatch(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const passwordConfirmation = control.get('passwordConfirmation')?.value;

  return password === passwordConfirmation ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-signup-form',
  imports: [ReactiveFormsModule],
  templateUrl: './signup-form.html',
  styleUrl: './signup-form.css',
})
export class SignupForm {
  readonly emailError = input<string | null>(null);

  readonly submitted = output<CreateUserRequest>();
  readonly cancelled = output<void>();

  readonly form = new FormGroup(
    {
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
          Validators.required,
          Validators.minLength(12),
          Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/),
        ],
      }),
      passwordConfirmation: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      termsAccepted: new FormControl(false, {
        nonNullable: true,
        validators: [Validators.requiredTrue],
      }),
    },
    {
      validators: [passwordsMatch],
    },
  );

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const {
      passwordConfirmation: _passwordConfirmation,
      termsAccepted: _termsAccepted,
      ...request
    } = this.form.getRawValue();

    this.submitted.emit(request);
  }

  cancel(): void {
    this.cancelled.emit();
  }
}
