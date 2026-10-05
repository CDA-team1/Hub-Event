import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AccountApi } from '../../../core/accounts/account-api';
import { ClubApi } from '../../../core/clubs/club-api';
import { AccountDto, AdminUserDto } from '../../../domain/account.model';
import { AdminUserRequest } from '../../../domain/admin-user-request';
import { ClubDto } from '../../../domain/club.model';
import { PageDto } from '../../../domain/page.model';
import { UserFormPage } from './user-form-page';

describe('UserFormPage', () => {
  const activeClub: ClubDto = {
    id: 1,
    name: 'Club Sport',
    category: 'SPORT',
    postalAddress: '1 rue du Sport',
    email: 'sport@test.com',
    phone: '0600000001',
    validityEndDate: null,
    members: [],
  };

  const endedClub: ClubDto = {
    id: 2,
    name: 'Ancien Club',
    category: 'CULTURE',
    postalAddress: '2 rue de la Culture',
    email: 'culture@test.com',
    phone: '0600000002',
    validityEndDate: '2026-09-01',
    members: [],
  };

  const secondActiveClub: ClubDto = {
    id: 3,
    name: 'Club Loisirs',
    category: 'LEISURE',
    postalAddress: '3 rue des Loisirs',
    email: 'loisirs@test.com',
    phone: '0600000003',
    validityEndDate: null,
    members: [],
  };

  const firstClubPage: PageDto<ClubDto> = {
    content: [activeClub, endedClub],
    page: 0,
    size: 20,
    totalElements: 3,
    totalPages: 2,
    first: true,
    last: false,
  };

  const secondClubPage: PageDto<ClubDto> = {
    content: [secondActiveClub],
    page: 1,
    size: 20,
    totalElements: 3,
    totalPages: 2,
    first: false,
    last: true,
  };

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
        id: 1,
        name: 'Club Sport',
      },
      {
        id: 2,
        name: 'Ancien Club',
      },
    ],
  };

  const savedAccount: AccountDto = {
    id: 12,
    lastName: 'Martin',
    firstName: 'Alice',
    postalAddress: '5 rue des Lilas',
    email: 'alice@test.com',
    phone: '0612345678',
    status: 'ACTIVE',
    role: 'MEMBER',
  };

  const request: AdminUserRequest = {
    lastName: 'Martin',
    firstName: 'Alice',
    postalAddress: '5 rue des Lilas',
    email: 'alice@test.com',
    phone: '0612345678',
    role: 'MEMBER',
    clubIds: [1],
  };

  let accountApi: {
    getUserById: ReturnType<typeof vi.fn>;
    createUser: ReturnType<typeof vi.fn>;
    updateUser: ReturnType<typeof vi.fn>;
  };

  let clubApi: {
    getClubs: ReturnType<typeof vi.fn>;
  };

  let route: {
    snapshot: {
      paramMap: ReturnType<typeof convertToParamMap>;
    };
  };

  let router: {
    navigate: ReturnType<typeof vi.fn>;
  };

  const stable = async () => {
    await TestBed.inject(ApplicationRef).whenStable();
  };

  beforeEach(() => {
    accountApi = {
      getUserById: vi.fn().mockReturnValue(of(user)),
      createUser: vi.fn().mockReturnValue(of(savedAccount)),
      updateUser: vi.fn().mockReturnValue(of(savedAccount)),
    };

    clubApi = {
      getClubs: vi.fn().mockImplementation((page: number) => {
        return page === 0 ? of(firstClubPage) : of(secondClubPage);
      }),
    };

    route = {
      snapshot: {
        paramMap: convertToParamMap({}),
      },
    };

    router = {
      navigate: vi.fn().mockResolvedValue(true),
    };

    TestBed.configureTestingModule({
      providers: [
        {
          provide: AccountApi,
          useValue: accountApi,
        },
        {
          provide: ClubApi,
          useValue: clubApi,
        },
        {
          provide: ActivatedRoute,
          useValue: route,
        },
        {
          provide: Router,
          useValue: router,
        },
      ],
    });
  });

  async function createPage(id: string | null = null) {
    route.snapshot.paramMap = convertToParamMap(id === null ? {} : { id });

    const fixture = TestBed.createComponent(UserFormPage);

    fixture.detectChanges();

    await stable();

    return {
      component: fixture.componentInstance,
      element: fixture.nativeElement as HTMLElement,
    };
  }

  it('charge toutes les pages de clubs et exclut les clubs terminés', async () => {
    const { component } = await createPage();

    expect(clubApi.getClubs).toHaveBeenNthCalledWith(1, 0, 20);
    expect(clubApi.getClubs).toHaveBeenNthCalledWith(2, 1, 20);

    expect(component.clubs()).toEqual([activeClub, secondActiveClub]);
    expect(component.loading()).toBe(false);
  });

  it('crée un compte puis revient à la liste des comptes', async () => {
    const { component } = await createPage();

    component.save(request);

    expect(accountApi.createUser).toHaveBeenCalledWith(request);
    expect(accountApi.updateUser).not.toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/admin/comptes']);
  });

  it('charge le compte et son rôle en modification', async () => {
    const { component } = await createPage('12');

    expect(accountApi.getUserById).toHaveBeenCalledWith(12);
    expect(component.editing()).toBe(true);
    expect(component.selectedRole()).toBe('MEMBER');

    expect(component.user()?.clubs).toEqual([
      {
        id: 1,
        name: 'Club Sport',
      },
    ]);
  });

  it('modifie un compte puis revient à la liste des comptes', async () => {
    const { component } = await createPage('12');

    component.save(request);

    expect(accountApi.updateUser).toHaveBeenCalledWith(12, request);
    expect(accountApi.createUser).not.toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/admin/comptes']);
  });

  it('refuse un identifiant de compte invalide sans charger les données', async () => {
    const { component, element } = await createPage('abc');

    expect(component.invalidId()).toBe(true);
    expect(component.loadErrorMessage()).toBe('Identifiant de compte invalide.');

    expect(clubApi.getClubs).not.toHaveBeenCalled();
    expect(accountApi.getUserById).not.toHaveBeenCalled();

    expect(element.textContent).toContain('Identifiant de compte invalide.');
    expect(element.textContent).not.toContain('Réessayer');
  });

  it('affiche une erreur et permet de réessayer si le chargement des clubs échoue', async () => {
    clubApi.getClubs.mockReturnValue(throwError(() => new Error('Erreur serveur')));

    const { component, element } = await createPage();

    expect(component.loadErrorMessage()).toBe('Impossible de charger la liste des clubs.');

    expect(element.textContent).toContain('Impossible de charger la liste des clubs.');
    expect(element.textContent).toContain('Réessayer');
  });

  it('affiche sous le champ email une erreur email renvoyée lors de la création', async () => {
    accountApi.createUser.mockReturnValue(
      throwError(() => ({
        status: 400,
        error: 'Cette adresse email est déjà utilisée.',
      })),
    );

    const { component } = await createPage();

    component.save(request);

    expect(component.emailError()).toBe('Cette adresse email est déjà utilisée.');
    expect(component.saveErrorMessage()).toBeNull();
  });
});
