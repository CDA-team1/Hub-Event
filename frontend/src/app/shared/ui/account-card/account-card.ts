import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AccountDto } from '../../../domain/account.model';
import { RoleBadge } from '../role-badge/role-badge';

@Component({
  selector: 'app-account-card',
  imports: [RouterLink, RoleBadge],
  templateUrl: './account-card.html',
  styleUrl: './account-card.css',
})
export class AccountCard {
  readonly user = input.required<AccountDto>();

  readonly suspendClicked = output<number>();

  suspend(): void {
    this.suspendClicked.emit(this.user().id);
  }
}
