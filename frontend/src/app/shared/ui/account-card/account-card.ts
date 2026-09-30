import { Component, computed, input, output } from '@angular/core';

import { AccountCard as AccountCardModel } from '../../../core/accounts/account-card';
import { Card, CardData } from '../card/card';

@Component({
  selector: 'app-account-card',
  imports: [Card],
  templateUrl: './account-card.html',
  styleUrl: './account-card.css',
})
export class AccountCard {
  readonly user = input.required<AccountCardModel>();

  readonly suspendClicked = output<number>();

  readonly cardData = computed<CardData>(() => ({
    title: `${this.user().firstName} ${this.user().lastName}`,
    information: [
      this.user().email,
      this.user().phone ?? 'Téléphone non renseigné',
      this.user().role,
      this.user().status,
    ],
    actionLabel: 'Suspendre',
  }));

  suspend(): void {
    this.suspendClicked.emit(this.user().id);
  }
}
