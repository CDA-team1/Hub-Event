import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { AccountApi } from '../../../core/accounts/account-api';
import { Confirmation } from '../../../core/dialog/confirmation';
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
    deleteUser: ReturnType<typeof vi.fn>;
  };
  let confirmation: { confirm: ReturnType<typeof vi.fn> };

  const stable = async () => {
    await TestBed.inject(ApplicationRef).whenStable();
  };

  beforeEach(() => {
    accountApi = {
      getUsers: vi.fn().mockReturnValue(of(page)),
      deleteUser: vi.fn().mockReturnValue(of(undefined)),
    };
    confirmation = { confirm: vi.fn().mockResolvedValue(true) };

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AccountApi,
          useValue: accountApi,
        },
        { provide: Confirmation, useValue: confirmation },
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

  it('propose Suspendre, qui mène à la page de suspension du compte', async () => {
    const router = TestBed.inject(Router);
    await router.navigateByUrl('/');

    const fixture = TestBed.createComponent(UsersListPage);

    await stable();

    const element = fixture.nativeElement as HTMLElement;
    const link = element.querySelector<HTMLAnchorElement>('a[aria-label="Suspendre"]');

    expect(link?.getAttribute('href')).toBe('/admin/comptes/1/suspension');
  });

  describe('suppression d’un compte', () => {
    const macrotask = () => new Promise((resolve) => setTimeout(resolve));

    async function clickDelete(): Promise<HTMLElement> {
      const fixture = TestBed.createComponent(UsersListPage);
      await stable();

      const element = fixture.nativeElement as HTMLElement;
      element.querySelector<HTMLButtonElement>('button[aria-label="Supprimer"]')!.click();
      await macrotask();
      await stable();

      return element;
    }

    it('demande confirmation, supprime le compte puis recharge la liste', async () => {
      await clickDelete();

      expect(confirmation.confirm).toHaveBeenCalledTimes(1);
      expect(accountApi.deleteUser).toHaveBeenCalledWith(1);
      expect(accountApi.getUsers).toHaveBeenCalledTimes(2);
    });

    it('ne supprime rien quand la confirmation est refusée', async () => {
      confirmation.confirm.mockResolvedValue(false);

      await clickDelete();

      expect(accountApi.deleteUser).not.toHaveBeenCalled();
    });

    it('affiche le message du back quand la suppression est refusée', async () => {
      accountApi.deleteUser.mockReturnValue(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 400,
              error: 'Ce compte possède un historique : utilisez l’anonymisation.',
            }),
        ),
      );

      const element = await clickDelete();

      expect(element.querySelector('[role="alert"]')?.textContent).toContain(
        'utilisez l’anonymisation',
      );
      expect(accountApi.getUsers).toHaveBeenCalledTimes(1);
    });

    it('affiche un message générique quand la suppression échoue sans message', async () => {
      accountApi.deleteUser.mockReturnValue(throwError(() => new Error('réseau')));

      const element = await clickDelete();

      expect(element.querySelector('[role="alert"]')?.textContent).toContain(
        'La suppression a échoué, réessayez.',
      );
    });
  });

  it('reprend le prénom, l’email et le statut sous le nom', async () => {
    const fixture = TestBed.createComponent(UsersListPage);

    await stable();

    const meta = (fixture.nativeElement as HTMLElement).querySelector('tbody td .user-meta')!;

    expect(meta.textContent).toContain('Alice');
    expect(meta.textContent).toContain('alice@test.com');
    expect(meta.textContent).toContain('Actif');
  });

  it('affiche le tableau avec une grille complète', async () => {
    const fixture = TestBed.createComponent(UsersListPage);

    await stable();

    const table = (fixture.nativeElement as HTMLElement).querySelector('app-data-table')!;

    expect(table.classList.contains('bordered')).toBe(true);
  });
});
