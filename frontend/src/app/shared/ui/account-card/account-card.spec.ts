import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { AccountDto } from '../../../domain/account.model';
import { AccountCard } from './account-card';

describe('AccountCard', () => {
  const user: AccountDto = {
    id: 12,
    lastName: 'Martin',
    firstName: 'Alice',
    postalAddress: '12 rue des Tests',
    email: 'alice.martin@test.com',
    phone: '0612345678',
    status: 'ACTIVE',
    role: 'MEMBER',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountCard],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('affiche les informations du compte', () => {
    const fixture = TestBed.createComponent(AccountCard);

    fixture.componentRef.setInput('user', user);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Martin');
    expect(text).toContain('Alice');
    expect(text).toContain('alice.martin@test.com');
    expect(text).toContain('Actif');
  });

  it('émet l’identifiant du compte au clic sur Suspendre', () => {
    const fixture = TestBed.createComponent(AccountCard);
    const emitted: number[] = [];

    fixture.componentRef.setInput('user', user);
    fixture.componentInstance.suspendClicked.subscribe((id) => emitted.push(id));
    fixture.detectChanges();

    fixture.nativeElement.querySelector('button').click();

    expect(emitted).toEqual([12]);
  });

  it('construit le lien de modification avec l’identifiant du compte', () => {
    const fixture = TestBed.createComponent(AccountCard);

    fixture.componentRef.setInput('user', user);
    fixture.detectChanges();

    const link = fixture.nativeElement.querySelector('a') as HTMLAnchorElement;

    expect(link.getAttribute('href')).toBe('/admin/comptes/12');
  });
});
