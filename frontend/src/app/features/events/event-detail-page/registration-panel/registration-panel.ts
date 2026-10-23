import { Component, computed, input, output } from '@angular/core';

import { EventDetailResponse } from '../../../../domain/event.model';

@Component({
  selector: 'app-registration-panel',
  templateUrl: './registration-panel.html',
  styleUrl: './registration-panel.css',
})
export class RegistrationPanel {
  readonly event = input.required<EventDetailResponse>();
  readonly pending = input(false);
  readonly errorMessage = input('');

  readonly registerClicked = output<void>();
  readonly unregisterClicked = output<void>();

  protected readonly isOpen = computed(() => this.event().status === 'PUBLISHED');
}
