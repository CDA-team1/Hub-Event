import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

import { AccountApi } from '../../../core/accounts/account-api';
import { API_URL } from '../../../core/http/api-url';
import { ConfirmAccountCreationRequest } from '../../../domain/confirm-account-creation-request';
import { ConfirmAccountCreationPage } from './confirm-account-creation-page';

describe('ConfirmAccountCreationPage', () => {
  let http: HttpTestingController | null = null;

  const request: ConfirmAccountCreationRequest = {
    temporaryPassword: 'Temporaire123!',
    newPassword: 'NouveauSecret123!',
    confirmPassword: 'NouveauSecret123!',
  };

  const stable = async () => {
    await TestBed.inject(ApplicationRef).whenStable();
  };

  async function createPage(token: string | null) {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AccountApi,
        {
          provide: API_URL,
          useValue: '/api',
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap(token ? { token } : {}),
            },
          },
        },
      ],
    });

    const fixture = TestBed.createComponent(ConfirmAccountCreationPage);

    http = TestBed.inject(HttpTestingController);

    await stable();

    return {
      component: fixture.componentInstance,
      element: fixture.nativeElement as HTMLElement,
    };
  }

  afterEach(() => {
    http?.verify();
    http = null;
  });

  it("affiche une erreur si le lien ne contient pas de token et n'appelle pas l'API", async () => {
    const { component, element } = await createPage(null);

    expect(component.invalidLink).toBe(true);
    expect(element.textContent).toContain('Le lien de confirmation est invalide.');

    component.confirm(request);

    http!.expectNone('/api/auth/confirm-account-creation');
  });

  it('envoie le token et les mots de passe au backend', async () => {
    const { component } = await createPage('token-de-test');

    component.confirm(request);

    const req = http!.expectOne(
      (request) =>
        request.url === '/api/auth/confirm-account-creation' &&
        request.params.get('token') === 'token-de-test',
    );

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);

    req.flush({});
    await stable();

    expect(component.success()).toBe(true);
    expect(component.loading()).toBe(false);
  });

  it('affiche le message de succès lorsque le compte est activé', async () => {
    const { component, element } = await createPage('token-de-test');

    component.confirm(request);

    http!
      .expectOne(
        (request) =>
          request.url === '/api/auth/confirm-account-creation' &&
          request.params.get('token') === 'token-de-test',
      )
      .flush({});

    await stable();

    expect(element.textContent).toContain('Votre compte est maintenant actif.');
  });

  it('affiche le message dédié si le mot de passe temporaire est incorrect', async () => {
    const { component, element } = await createPage('token-de-test');

    component.confirm(request);

    http!
      .expectOne(
        (request) =>
          request.url === '/api/auth/confirm-account-creation' &&
          request.params.get('token') === 'token-de-test',
      )
      .flush('Mot de passe temporaire incorrect.', {
        status: 400,
        statusText: 'Bad Request',
      });

    await stable();

    expect(component.loading()).toBe(false);
    expect(component.errorMessage()).toBe('Mot de passe temporaire incorrect.');
    expect(element.textContent).toContain('Mot de passe temporaire incorrect.');
  });

  it('affiche un message générique pour une erreur inattendue', async () => {
    const { component, element } = await createPage('token-de-test');

    component.confirm(request);

    http!
      .expectOne(
        (request) =>
          request.url === '/api/auth/confirm-account-creation' &&
          request.params.get('token') === 'token-de-test',
      )
      .flush('Erreur interne', {
        status: 500,
        statusText: 'Internal Server Error',
      });

    await stable();

    expect(component.errorMessage()).toBe(
      'Une erreur est survenue lors de la confirmation du compte.',
    );
    expect(element.textContent).toContain(
      'Une erreur est survenue lors de la confirmation du compte.',
    );
  });
});
