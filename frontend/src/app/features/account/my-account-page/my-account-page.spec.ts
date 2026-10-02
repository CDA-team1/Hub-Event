import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AccountApi } from '../../../core/accounts/account-api';
import { Auth } from '../../../core/auth/auth';
import { AccountDto, UserProfileDto } from '../../../domain/account.model';
import { UpdateUserRequest } from '../../../domain/update-user-request';
import { MyAccountPage } from './my-account-page';

describe('MyAccountPage', () => {
  const profile: UserProfileDto = {
    id: 1,
    lastName: 'Doe',
    firstName: 'John',
    postalAddress: '1 rue de Test',
    email: 'john.doe@test.com',
    phone: '0600000000',
    status: 'ACTIVE',
    role: 'MEMBER',
    clubs: [{ id: 1, name: 'Club de Test' }],
  };

  const updatedAccount: AccountDto = {
    id: 1,
    lastName: 'Doe',
    firstName: 'Jonathan',
    postalAddress: 'Nouvelle adresse',
    email: 'john.doe@test.com',
    phone: '0600000000',
    status: 'ACTIVE',
    role: 'MEMBER',
  };

  let accountApi: {
    getOwnAccount: ReturnType<typeof vi.fn>;
    updateOwnAccount: ReturnType<typeof vi.fn>;
  };

  let auth: {
    logout: ReturnType<typeof vi.fn>;
  };

  let router: {
    navigate: ReturnType<typeof vi.fn>;
  };

  const stable = async () => {
    await TestBed.inject(ApplicationRef).whenStable();
  };

  beforeEach(() => {
    accountApi = {
      getOwnAccount: vi.fn().mockReturnValue(of(profile)),
      updateOwnAccount: vi.fn().mockReturnValue(of(updatedAccount)),
    };

    auth = {
      logout: vi.fn(),
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
          provide: Auth,
          useValue: auth,
        },
        {
          provide: Router,
          useValue: router,
        },
      ],
    });
  });

  async function createPage() {
    const fixture = TestBed.createComponent(MyAccountPage);

    await stable();

    return {
      component: fixture.componentInstance,
      element: fixture.nativeElement as HTMLElement,
    };
  }

  it('charge et affiche le profil de l’utilisateur', async () => {
    const { component, element } = await createPage();

    expect(accountApi.getOwnAccount).toHaveBeenCalledOnce();
    expect(component.profile()).toEqual(profile);
    expect(component.loading()).toBe(false);

    expect(element.textContent).toContain('Modifier mon compte');
    expect(element.textContent).toContain('Club de Test');
  });

  it('met à jour le compte sans changement de mot de passe', async () => {
    const { component } = await createPage();

    const request: UpdateUserRequest = {
      lastName: 'Doe',
      firstName: 'Jonathan',
      postalAddress: 'Nouvelle adresse',
      email: 'john.doe@test.com',
      phone: '0600000000',
      password: null,
    };

    component.update(request);

    expect(accountApi.updateOwnAccount).toHaveBeenCalledWith(request);
    expect(component.successMessage()).toBe('Vos informations ont été mises à jour.');
    expect(component.profile()?.firstName).toBe('Jonathan');
  });

  it('indique qu’un email de confirmation est envoyé pour le mot de passe', async () => {
    const { component } = await createPage();

    const request: UpdateUserRequest = {
      lastName: 'Doe',
      firstName: 'John',
      postalAddress: '1 rue de Test',
      email: 'john.doe@test.com',
      phone: '0600000000',
      password: 'NouveauSecret123!',
    };

    component.update(request);

    expect(component.successMessage()).toContain('Un email de confirmation a été envoyé');
  });

  it('affiche l’erreur email renvoyée par le backend', async () => {
    accountApi.updateOwnAccount.mockReturnValue(
      throwError(() => ({
        status: 400,
        error: 'Cette adresse email est déjà utilisée.',
      })),
    );

    const { component } = await createPage();

    component.update({
      lastName: 'Doe',
      firstName: 'John',
      postalAddress: '1 rue de Test',
      email: 'taken@test.com',
      phone: null,
      password: null,
    });

    expect(component.emailError()).toBe('Cette adresse email est déjà utilisée.');
  });

  it('déconnecte après un changement d’email réussi', async () => {
    const { component } = await createPage();

    component.update({
      lastName: 'Doe',
      firstName: 'John',
      postalAddress: '1 rue de Test',
      email: 'nouveau@test.com',
      phone: '0600000000',
      password: null,
    });

    expect(auth.logout).toHaveBeenCalledOnce();
    expect(router.navigate).toHaveBeenCalledWith(['/connexion']);
  });

  it('revient à l’accueil lorsque l’utilisateur annule', async () => {
    const { component } = await createPage();

    component.cancel();

    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });
});
