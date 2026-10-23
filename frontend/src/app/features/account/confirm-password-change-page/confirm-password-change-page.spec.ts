import { HttpErrorResponse } from '@angular/common/http';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AccountApi } from '../../../core/accounts/account-api';
import { ConfirmPasswordChangePage } from './confirm-password-change-page';

describe('ConfirmPasswordChangePage', () => {
  let accountApi: { confirmPasswordChange: ReturnType<typeof vi.fn> };

  const stable = () => TestBed.inject(ApplicationRef).whenStable();

  function setup(token: string | null) {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AccountApi, useValue: accountApi },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: () => token } } },
        },
      ],
    });
    return TestBed.createComponent(ConfirmPasswordChangePage);
  }

  beforeEach(() => {
    accountApi = { confirmPasswordChange: vi.fn().mockReturnValue(of({})) };
  });

  it('confirme le changement avec le jeton du lien et propose de se connecter', async () => {
    const fixture = setup('jeton-du-lien');
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    expect(accountApi.confirmPasswordChange).toHaveBeenCalledWith('jeton-du-lien');
    expect(element.textContent).toContain('Mot de passe modifié');
    expect(element.querySelector('a')?.getAttribute('href')).toBe('/connexion');
  });

  it('affiche le message du back quand le lien est refusé (expiré, déjà utilisé…)', async () => {
    accountApi.confirmPasswordChange.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error:
              'Ce lien a expiré. Refaites la modification du mot de passe depuis votre compte.',
          }),
      ),
    );

    const fixture = setup('jeton-expire');
    await stable();

    const alert = (fixture.nativeElement as HTMLElement).querySelector('[role="alert"]');
    expect(alert?.textContent).toContain('Ce lien a expiré');
  });

  it('affiche un message générique quand le serveur répond autrement', async () => {
    accountApi.confirmPasswordChange.mockReturnValue(
      throwError(
        () => new HttpErrorResponse({ status: 500, error: 'Une erreur interne est survenue.' }),
      ),
    );

    const fixture = setup('jeton');
    await stable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Une erreur est survenue lors de la confirmation du mot de passe.',
    );
  });

  it("n'appelle pas le serveur quand le lien n'a pas de jeton", async () => {
    const fixture = setup(null);
    await stable();

    expect(accountApi.confirmPasswordChange).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Le lien de confirmation est invalide.',
    );
  });
});
