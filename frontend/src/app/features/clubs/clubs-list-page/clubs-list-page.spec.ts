import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Confirmation } from '../../../core/dialog/confirmation';
import { ClubDto } from '../../../domain/club.model';
import { PageDto } from '../../../domain/page.model';
import { ClubsListPage } from './clubs-list-page';

function makeClub(id: number, name: string): ClubDto {
  return {
    id,
    name,
    category: 'SPORT',
    postalAddress: '1 rue de Test',
    email: `${name.toLowerCase()}@test.fr`,
    phone: '0600000000',
    validityEndDate: null,
    members: [],
  };
}

function pageOf(content: ClubDto[], overrides: Partial<PageDto<ClubDto>> = {}): PageDto<ClubDto> {
  return {
    content,
    page: 0,
    size: 20,
    totalElements: content.length,
    totalPages: 1,
    first: true,
    last: true,
    ...overrides,
  };
}

describe('ClubsListPage', () => {
  let http: HttpTestingController;

  const stable = () => TestBed.inject(ApplicationRef).whenStable();

  // Un `await` supplémentaire (macrotâche) laisse le temps à la chaîne confirm() → delete() →
  // reload() de se terminer avant whenStable() (voir calendar-page.spec.ts pour la même astuce).
  const macrotask = () => new Promise((resolve) => setTimeout(resolve));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('affiche un indicateur de chargement avant la réponse', () => {
    const fixture = TestBed.createComponent(ClubsListPage);
    TestBed.tick();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-loading-state')).not.toBeNull();

    http.expectOne((req) => req.url.endsWith('/clubs')).flush(pageOf([]));
  });

  it("affiche un état vide quand il n'y a aucun club", async () => {
    const fixture = TestBed.createComponent(ClubsListPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/clubs')).flush(pageOf([]));
    await stable();

    expect((fixture.nativeElement as HTMLElement).querySelector('app-empty-state')).not.toBeNull();
  });

  it('affiche les clubs dans un tableau, avec le badge de catégorie', async () => {
    const fixture = TestBed.createComponent(ClubsListPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/clubs')).flush(pageOf([makeClub(1, 'Club Alpha')]));
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Club Alpha');
    expect(element.querySelector('app-category-badge')?.textContent?.trim()).toBe('Sport');
  });

  it('relance la requête avec la nouvelle page au clic sur Suivant', async () => {
    const fixture = TestBed.createComponent(ClubsListPage);
    TestBed.tick();
    http
      .expectOne((req) => req.url.endsWith('/clubs'))
      .flush(pageOf([makeClub(1, 'Club Alpha')], { totalElements: 25, totalPages: 2 }));
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('app-pagination button:last-child')!.click();
    TestBed.tick();

    const request = http.expectOne((req) => req.url.endsWith('/clubs'));
    expect(request.request.params.get('page')).toBe('1');
    request.flush(pageOf([makeClub(2, 'Club Bravo')], { page: 1, totalElements: 25, totalPages: 2 }));
    await stable();

    expect(element.textContent).toContain('Club Bravo');
  });

  it('supprime le club après confirmation et recharge la liste', async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(ClubsListPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/clubs')).flush(pageOf([makeClub(1, 'Club Alpha')]));
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('app-action-button button')!.click();
    await stable();

    http.expectOne((req) => req.method === 'DELETE' && req.url.endsWith('/clubs/1')).flush(null);
    await macrotask();
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/clubs')).flush(pageOf([]));
    await stable();

    expect(element.querySelector('app-empty-state')).not.toBeNull();
  });

  it('ne supprime pas si on annule la confirmation', async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(false);

    const fixture = TestBed.createComponent(ClubsListPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/clubs')).flush(pageOf([makeClub(1, 'Club Alpha')]));
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('app-action-button button')!.click();
    await stable();

    expect(element.textContent).toContain('Club Alpha');
  });

  it('affiche une erreur si la suppression échoue', async () => {
    vi.spyOn(TestBed.inject(Confirmation), 'confirm').mockResolvedValue(true);

    const fixture = TestBed.createComponent(ClubsListPage);
    TestBed.tick();
    http.expectOne((req) => req.url.endsWith('/clubs')).flush(pageOf([makeClub(1, 'Club Alpha')]));
    await stable();

    const element = fixture.nativeElement as HTMLElement;
    element.querySelector<HTMLButtonElement>('app-action-button button')!.click();
    await stable();

    http
      .expectOne((req) => req.method === 'DELETE' && req.url.endsWith('/clubs/1'))
      .flush('Ce club est déjà supprimé.', { status: 400, statusText: 'Bad Request' });
    await stable();

    expect(element.textContent).toContain('La suppression a échoué, réessayez.');
  });
});
