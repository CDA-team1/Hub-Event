import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminUserDto } from '../../../domain/account.model';
import { ClubDto } from '../../../domain/club.model';
import { UserForm } from './user-form';

describe('UserForm', () => {
  let fixture: ComponentFixture<UserForm>;
  let component: UserForm;

  const clubs: ClubDto[] = [
    {
      id: 1,
      name: 'Club Sport',
      category: 'SPORT',
      postalAddress: '1 rue du Sport',
      email: 'sport@test.com',
      phone: '0600000001',
      validityEndDate: null,
      members: [],
    },
    {
      id: 2,
      name: 'Club Culture',
      category: 'CULTURE',
      postalAddress: '2 rue de la Culture',
      email: 'culture@test.com',
      phone: '0600000002',
      validityEndDate: null,
      members: [],
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserForm],
    }).compileComponents();

    fixture = TestBed.createComponent(UserForm);
    component = fixture.componentInstance;

    fixture.componentRef.setInput('role', 'MEMBER');
    fixture.componentRef.setInput('clubs', clubs);

    fixture.detectChanges();
  });

  function fillValidForm(): void {
    component.form.setValue({
      lastName: 'Doe',
      firstName: 'John',
      postalAddress: '1 rue de Test',
      email: 'john.doe@test.com',
      phone: '0600000000',
    });
  }

  it('affiche le sélecteur de clubs pour un membre', () => {
    expect(fixture.nativeElement.querySelector('app-club-affiliation-picker')).not.toBeNull();
  });

  it('n’affiche pas le sélecteur de clubs pour un administrateur', () => {
    fixture.componentRef.setInput('role', 'ADMIN');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('app-club-affiliation-picker')).toBeNull();
  });

  it('autorise un membre sans club', () => {
    fillValidForm();

    const emitted = vi.fn();
    component.submitted.subscribe(emitted);

    component.submit();

    expect(emitted).toHaveBeenCalledWith({
      lastName: 'Doe',
      firstName: 'John',
      postalAddress: '1 rue de Test',
      email: 'john.doe@test.com',
      phone: '0600000000',
      role: 'MEMBER',
      clubIds: [],
    });
  });

  it('refuse un organisateur sans club', () => {
    fixture.componentRef.setInput('role', 'ORGANIZER');
    fixture.detectChanges();

    fillValidForm();

    const emitted = vi.fn();
    component.submitted.subscribe(emitted);

    component.submit();

    expect(emitted).not.toHaveBeenCalled();
    expect(component.clubSelectionTouched()).toBe(true);
  });

  it('émet les clubs sélectionnés pour un organisateur', () => {
    fixture.componentRef.setInput('role', 'ORGANIZER');
    fixture.detectChanges();

    fillValidForm();
    component.updateClubSelection([1, 2]);

    const emitted = vi.fn();
    component.submitted.subscribe(emitted);

    component.submit();

    expect(emitted).toHaveBeenCalledWith({
      lastName: 'Doe',
      firstName: 'John',
      postalAddress: '1 rue de Test',
      email: 'john.doe@test.com',
      phone: '0600000000',
      role: 'ORGANIZER',
      clubIds: [1, 2],
    });
  });

  it('préremplit le formulaire et les clubs en modification', () => {
    const user: AdminUserDto = {
      id: 12,
      lastName: 'Martin',
      firstName: 'Alice',
      postalAddress: '5 rue des Lilas',
      email: 'alice@test.com',
      phone: '0612345678',
      status: 'ACTIVE',
      role: 'MEMBER',
      clubs: [
        {
          id: 2,
          name: 'Club Culture',
        },
      ],
    };

    fixture.componentRef.setInput('user', user);
    fixture.detectChanges();

    expect(component.form.getRawValue()).toEqual({
      lastName: 'Martin',
      firstName: 'Alice',
      postalAddress: '5 rue des Lilas',
      email: 'alice@test.com',
      phone: '0612345678',
    });

    expect(component.selectedClubIds()).toEqual([2]);
  });
});

