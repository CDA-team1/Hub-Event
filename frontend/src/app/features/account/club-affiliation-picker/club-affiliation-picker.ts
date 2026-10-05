import { Component, input, output } from '@angular/core';

import { ClubCardDto } from '../../../domain/club.model';

@Component({
  selector: 'app-club-affiliation-picker',
  templateUrl: './club-affiliation-picker.html',
  styleUrl: './club-affiliation-picker.css',
})
export class ClubAffiliationPicker {
  readonly clubs = input.required<ClubCardDto[]>();
  readonly selected = input<number[]>([]);

  readonly selectionChanged = output<number[]>();

  isSelected(clubId: number): boolean {
    return this.selected().includes(clubId);
  }

  toggleClub(clubId: number, checked: boolean): void {
    const currentSelection = this.selected();

    const nextSelection = checked
      ? [...currentSelection, clubId]
      : currentSelection.filter((id) => id !== clubId);

    this.selectionChanged.emit(nextSelection);
  }
}
