import { HttpErrorResponse } from '@angular/common/http';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LegalDocumentApi } from '../../../core/privacy/legal-document-api';
import { LegalDocumentDto, LegalDocumentType } from '../../../domain/legal-document.model';
import { LegalDocumentAdminPage } from './legal-document-admin-page';

describe('LegalDocumentAdminPage', () => {
  const rgpdDocument: LegalDocumentDto = {
    id: 1,
    type: 'RGPD',
    content: 'Politique RGPD existante',
    updatedAt: '2026-10-07T13:00:00',
  };

  const cguDocument: LegalDocumentDto = {
    id: 2,
    type: 'CGU',
    content: 'Conditions Générales d’Utilisation existantes',
    updatedAt: '2026-10-07T13:00:00',
  };

  let legalDocumentApi: {
    getByType: ReturnType<typeof vi.fn>;
    upsert: ReturnType<typeof vi.fn>;
  };

  let route: {
    snapshot: {
      data: {
        legalDocumentType: LegalDocumentType;
      };
    };
  };

  let router: {
    navigate: ReturnType<typeof vi.fn>;
  };

  const stable = async () => {
    await TestBed.inject(ApplicationRef).whenStable();
  };

  beforeEach(() => {
    legalDocumentApi = {
      getByType: vi.fn().mockReturnValue(of(rgpdDocument)),
      upsert: vi.fn().mockReturnValue(of(rgpdDocument)),
    };

    route = {
      snapshot: {
        data: {
          legalDocumentType: 'RGPD',
        },
      },
    };

    router = {
      navigate: vi.fn().mockResolvedValue(true),
    };

    TestBed.configureTestingModule({
      providers: [
        {
          provide: LegalDocumentApi,
          useValue: legalDocumentApi,
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

  async function createPage(type: LegalDocumentType = 'RGPD') {
    route.snapshot.data.legalDocumentType = type;

    const fixture = TestBed.createComponent(LegalDocumentAdminPage);

    fixture.detectChanges();
    await stable();
    fixture.detectChanges();

    return {
      fixture,
      component: fixture.componentInstance,
      element: fixture.nativeElement as HTMLElement,
    };
  }

  it('charge la politique RGPD existante', async () => {
    const { component, element } = await createPage('RGPD');

    expect(legalDocumentApi.getByType).toHaveBeenCalledWith('RGPD');
    expect(component.content.value).toBe('Politique RGPD existante');
    expect(element.textContent).toContain('Modifier la politique RGPD');
  });

  it('charge les CGU existantes', async () => {
    legalDocumentApi.getByType.mockReturnValue(of(cguDocument));

    const { component, element } = await createPage('CGU');

    expect(legalDocumentApi.getByType).toHaveBeenCalledWith('CGU');
    expect(component.content.value).toBe('Conditions Générales d’Utilisation existantes');
    expect(element.textContent).toContain('Modifier les Conditions Générales d’Utilisation');
  });

  it("affiche un formulaire vide si le document n'existe pas encore", async () => {
    legalDocumentApi.getByType.mockReturnValue(of(null));

    const { component, element } = await createPage();

    expect(component.loadErrorMessage()).toBeNull();
    expect(component.content.value).toBe('');
    expect(element.querySelector('textarea')).not.toBeNull();
  });

  it('affiche une erreur si le document ne peut pas être chargé', async () => {
    legalDocumentApi.getByType.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 500,
            statusText: 'Server Error',
          }),
      ),
    );

    const { component, element } = await createPage('RGPD');

    expect(component.loadErrorMessage()).toBe('Impossible de charger la politique RGPD.');
    expect(element.textContent).toContain('Impossible de charger la politique RGPD.');
  });

  it('enregistre le contenu modifié', async () => {
    const updatedDocument: LegalDocumentDto = {
      ...rgpdDocument,
      content: 'Nouvelle politique RGPD',
    };

    legalDocumentApi.upsert.mockReturnValue(of(updatedDocument));

    const { component } = await createPage();

    component.content.setValue('Nouvelle politique RGPD');
    component.save();

    expect(legalDocumentApi.upsert).toHaveBeenCalledWith('RGPD', 'Nouvelle politique RGPD');
    expect(component.content.value).toBe('Nouvelle politique RGPD');
    expect(component.saving()).toBe(false);
  });

  it("refuse d'enregistrer un contenu vide", async () => {
    const { fixture, component, element } = await createPage();

    component.content.setValue('   ');
    component.save();
    fixture.detectChanges();

    expect(legalDocumentApi.upsert).not.toHaveBeenCalled();
    expect(element.textContent).toContain('Le contenu du document est obligatoire.');
  });

  it("affiche l'erreur métier renvoyée par le backend", async () => {
    legalDocumentApi.upsert.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            statusText: 'Bad Request',
            error: 'Le contenu du document ne peut pas être vide.',
          }),
      ),
    );

    const { fixture, component, element } = await createPage();

    component.content.setValue('Nouveau contenu');
    component.save();
    fixture.detectChanges();

    expect(element.textContent).toContain('Le contenu du document ne peut pas être vide.');
  });

  it("revient à l'accueil lorsque l'administrateur annule", async () => {
    const { element } = await createPage();

    const cancelButton = Array.from(element.querySelectorAll('button')).find(
      (button) => button.textContent?.trim() === 'Annuler',
    );

    expect(cancelButton).toBeDefined();

    (cancelButton as HTMLButtonElement).click();

    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });
});
