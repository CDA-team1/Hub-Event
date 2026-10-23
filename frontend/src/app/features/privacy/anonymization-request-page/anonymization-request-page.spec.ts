import { HttpErrorResponse } from '@angular/common/http';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { Confirmation } from '../../../core/dialog/confirmation';
import { AnonymizationApi } from '../../../core/privacy/anonymization-api';
import { AnonymizationDto } from '../../../domain/anonymization.model';
import { AnonymizationRequestPage } from './anonymization-request-page';

describe('AnonymizationRequestPage', () => {
  const createdRequest: AnonymizationDto = {
    id: 1,
    user: {
      id: 20,
      lastName: 'Doe',
      firstName: 'Jane',
      postalAddress: '1 rue de Test',
      email: 'jane.doe@test.com',
      phone: '0600000000',
      status: 'ACTIVE',
      role: 'MEMBER',
    },
    status: 'PENDING',
    requestDate: '2026-10-06T16:00:00',
  };

  let anonymizationApi: {
    createRequest: ReturnType<typeof vi.fn>;
  };

  let confirmation: {
    confirm: ReturnType<typeof vi.fn>;
  };

  let router: {
    navigate: ReturnType<typeof vi.fn>;
  };

  const stable = async () => {
    await new Promise((resolve) => setTimeout(resolve));
    await TestBed.inject(ApplicationRef).whenStable();
  };

  beforeEach(() => {
    anonymizationApi = {
      createRequest: vi.fn().mockResolvedValue(createdRequest),
    };

    confirmation = {
      confirm: vi.fn().mockResolvedValue(true),
    };

    router = {
      navigate: vi.fn().mockResolvedValue(true),
    };

    TestBed.configureTestingModule({
      providers: [
        {
          provide: AnonymizationApi,
          useValue: anonymizationApi,
        },
        {
          provide: Confirmation,
          useValue: confirmation,
        },
        {
          provide: Router,
          useValue: router,
        },
      ],
    });
  });

  async function createPage() {
    const fixture = TestBed.createComponent(AnonymizationRequestPage);

    await stable();

    return {
      fixture,
      element: fixture.nativeElement as HTMLElement,
    };
  }

  it("affiche les conséquences de la demande d'anonymisation", async () => {
    const { element } = await createPage();

    expect(element.textContent).toContain("Demander l'anonymisation de mes données");
    expect(element.textContent).toContain(
      "La création de la demande n'anonymise pas immédiatement votre compte.",
    );
    expect(element.textContent).toContain('SUPPRIMÉ');
    expect(element.textContent).toContain('événements publiés à venir');
  });

  it("crée la demande après confirmation de l'utilisateur", async () => {
    const { element } = await createPage();

    element.querySelector<HTMLButtonElement>('.anonymization-page__submit')!.click();

    await stable();

    expect(confirmation.confirm).toHaveBeenCalledWith(
      "Confirmer votre demande d'anonymisation ? Elle devra ensuite être validée par un administrateur.",
      'Valider la demande',
      'Annuler',
    );
    expect(anonymizationApi.createRequest).toHaveBeenCalledOnce();
    expect(element.textContent).toContain("Votre demande d'anonymisation a bien été enregistrée.");
  });

  it("ne crée pas la demande si l'utilisateur annule la confirmation", async () => {
    confirmation.confirm.mockResolvedValue(false);

    const { element } = await createPage();

    element.querySelector<HTMLButtonElement>('.anonymization-page__submit')!.click();

    await stable();

    expect(anonymizationApi.createRequest).not.toHaveBeenCalled();
  });

  it("affiche l'erreur métier renvoyée par le backend", async () => {
    anonymizationApi.createRequest.mockRejectedValue(
      new HttpErrorResponse({
        status: 400,
        error: "Une demande d'anonymisation existe déjà.",
      }),
    );

    const { element } = await createPage();

    element.querySelector<HTMLButtonElement>('.anonymization-page__submit')!.click();

    await stable();

    expect(element.querySelector('[role="alert"]')?.textContent).toContain(
      "Une demande d'anonymisation existe déjà.",
    );
  });

  it("revient à l'accueil lorsque l'utilisateur annule", async () => {
    const { element } = await createPage();

    element.querySelector<HTMLButtonElement>('.anonymization-page__cancel')!.click();

    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });
});
