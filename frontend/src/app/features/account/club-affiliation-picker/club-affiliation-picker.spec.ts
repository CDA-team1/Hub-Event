import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubCardDto } from '../../../domain/club.model';
import { ClubAffiliationPicker } from './club-affiliation-picker';

describe('ClubAffiliationPicker', () => {
  let fixture: ComponentFixture<ClubAffiliationPicker>;
  let component: ClubAffiliationPicker;

  const clubs: ClubCardDto[] = [
    {
      id: 1,
      name: 'Club Sport',
      category: 'SPORT',
      postalAddress: '1 rue du Sport',
      email: 'sport@test.com',
      phone: '0600000001',
      validityEndDate: null,
    },
    {
      id: 2,
      name: 'Club Culture',
      category: 'CULTURE',
      postalAddress: '2 rue de la Culture',
      email: 'culture@test.com',
      phone: '0600000002',
      validityEndDate: null,
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClubAffiliationPicker],
    }).compileComponents();

    fixture = TestBed.createComponent(ClubAffiliationPicker);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('clubs', clubs);
    fixture.componentRef.setInput('selected', [2]);

    fixture.detectChanges();
  });

  it('affiche les clubs disponibles', () => {
    const labels = fixture.nativeElement.querySelectorAll('.club-affiliation-picker__item');

    expect(labels).toHaveLength(2);
    expect(fixture.nativeElement.textContent).toContain('Club Sport');
    expect(fixture.nativeElement.textContent).toContain('Club Culture');
  });

  it('coche les clubs déjà sélectionnés', () => {
    const checkboxes = fixture.nativeElement.querySelectorAll(
      'input[type="checkbox"]',
    ) as NodeListOf<HTMLInputElement>;

    expect(checkboxes[0].checked).toBe(false);
    expect(checkboxes[1].checked).toBe(true);
  });

  it('émet la nouvelle sélection quand un club est coché', () => {
    const emittedSelections: number[][] = [];

    component.selectionChanged.subscribe((selection) => {
      emittedSelections.push(selection);
    });

    component.toggleClub(1, true);

    expect(emittedSelections).toEqual([[2, 1]]);
  });

  it('émet la nouvelle sélection quand un club est décoché', () => {
    const emittedSelections: number[][] = [];

    component.selectionChanged.subscribe((selection) => {
      emittedSelections.push(selection);
    });

    component.toggleClub(2, false);

    expect(emittedSelections).toEqual([[]]);
  });
});
