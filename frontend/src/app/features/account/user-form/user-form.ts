import { Component, computed, effect, input, output, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { AdminUserDto } from '../../../domain/account.model';
import { AdminUserRequest } from '../../../domain/admin-user-request';
import { ClubDto } from '../../../domain/club.model';
import { Role } from '../../../domain/role';
import { ClubAffiliationPicker } from '../club-affiliation-picker/club-affiliation-picker';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, ClubAffiliationPicker],
  templateUrl: './user-form.html',
  styleUrl: './user-form.css',
})
export class UserForm {
  readonly role = input.required<Role>();
  readonly user = input<AdminUserDto | null>(null);
  readonly clubs = input.required<ClubDto[]>();
  readonly emailError = input<string | null>(null);

  readonly submitted = output<AdminUserRequest>();
  readonly cancelled = output<void>();

  readonly selectedClubIds = signal<number[]>([]);
  readonly clubSelectionTouched = signal(false);

  readonly showClubPicker = computed(() => this.role() !== 'ADMIN');
  readonly clubRequired = computed(() => this.role() === 'ORGANIZER');

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
    phone: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  private readonly syncUser = effect(() => {
    const user = this.user();

    if (user === null) {
      this.form.reset({
        lastName: '',
        firstName: '',
        postalAddress: '',
        email: '',
        phone: '',
      });

      this.selectedClubIds.set([]);
      this.clubSelectionTouched.set(false);
      return;
    }

    this.form.patchValue({
      lastName: user.lastName,
      firstName: user.firstName,
      postalAddress: user.postalAddress,
      email: user.email,
      phone: user.phone ?? '',
    });

    this.selectedClubIds.set(user.clubs.map((club) => club.id));
    this.clubSelectionTouched.set(false);
  });

  updateClubSelection(clubIds: number[]): void {
    this.selectedClubIds.set(clubIds);
    this.clubSelectionTouched.set(true);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.clubRequired() && this.selectedClubIds().length === 0) {
      this.clubSelectionTouched.set(true);
      return;
    }

    const value = this.form.getRawValue();

    this.submitted.emit({
      lastName: value.lastName,
      firstName: value.firstName,
      postalAddress: value.postalAddress,
      email: value.email,
      phone: value.phone.trim(),
      role: this.role(),
      clubIds: this.showClubPicker() ? this.selectedClubIds() : [],
    });
  }

  cancel(): void {
    this.cancelled.emit();
  }
}

