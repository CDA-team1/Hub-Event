import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AccountApi } from '../../../core/accounts/account-api';
import { AccountDto } from '../../../domain/account.model';
import { PageDto } from '../../../domain/page.model';
import { UsersListPage } from './users-list-page';

describe('UsersListPage', () => {
  const user: AccountDto = {
    id: 1,
    lastName: 'Martin',
    firstName: 'Alice',
    postalAddress: '1 rue de Test',
    email: 'alice@test.com',
    phone: '0600000000',
    status: 'ACTIVE',
    role: 'MEMBER',
  };

  const page: PageDto<AccountDto> = {
    content: [user],
    page: 0,
    size: 20,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
  };

  let accountApi: {
    getUsers: ReturnType<typeof vi.fn>;
  };

  const stable = async () => {
    await TestBed.inject(ApplicationRef).whenStable();
  };

  beforeEach(() => {
    accountApi = {
      getUsers: vi.fn().mockReturnValue(of(page)),
    };

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AccountApi,
          useValue: accountApi,
        },
      ],
    });
  });

  it('charge et affiche la liste des comptes', async () => {
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/');

    const fixture = TestBed.createComponent(UsersListPage);

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    expect(accountApi.getUsers).toHaveBeenCalledWith(null, 0);
    expect(element.textContent).toContain('Martin');
    expect(element.textContent).toContain('alice@test.com');
  });

  it('affiche les comptes dans la table dynamique avec l’action Modifier', async () => {
    const fixture = TestBed.createComponent(UsersListPage);

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-data-table')).not.toBeNull();

    const modifyAction = element.querySelector('app-action-button[label="Modifier"]');

    expect(modifyAction).not.toBeNull();
  });

  it('lit le rôle et la page depuis les query params', async () => {
    const router = TestBed.inject(Router);

    await router.navigateByUrl('/?role=ADMIN&page=2');

    TestBed.createComponent(UsersListPage);

    await stable();

    expect(accountApi.getUsers).toHaveBeenCalledWith('ADMIN', 2);
  });

  it('met la page à zéro lors d’un changement de rôle', async () => {
    const router = TestBed.inject(Router);

    await router.navigateByUrl('/?page=3');

    const fixture = TestBed.createComponent(UsersListPage);

    await stable();

    const select = fixture.nativeElement.querySelector('select') as HTMLSelectElement;

    select.value = 'ADMIN';
    select.dispatchEvent(new Event('change'));

    await stable();

    expect(router.url).toContain('role=ADMIN');
    expect(router.url).toContain('page=0');
    expect(accountApi.getUsers).toHaveBeenCalledWith('ADMIN', 0);
  });

  it('affiche un état vide lorsqu’aucun compte n’est retourné', async () => {
    accountApi.getUsers.mockReturnValue(
      of({
        content: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true,
      }),
    );

    const fixture = TestBed.createComponent(UsersListPage);

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.textContent).toContain('Aucun compte ne correspond à ce filtre.');
  });

  it('affiche une erreur si le chargement échoue', async () => {
    accountApi.getUsers.mockReturnValue(throwError(() => new Error('Erreur serveur')));

    const fixture = TestBed.createComponent(UsersListPage);

    await stable();

    const element = fixture.nativeElement as HTMLElement;

    expect(element.textContent).toContain('Impossible de charger la liste des comptes.');
  });
});
