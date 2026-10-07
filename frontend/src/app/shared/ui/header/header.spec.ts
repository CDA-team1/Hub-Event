import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Header } from './header';

function signIn(role: 'MEMBER' | 'ADMIN' | 'ORGANIZER'): void {
  sessionStorage.setItem('hub-event.session.v1', JSON.stringify({ token: 'jwt-de-test', role }));
}

describe('Header', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  afterEach(() => sessionStorage.clear());

  async function render() {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('ne montre pas le menu Administration à un visiteur non connecté', async () => {
    const element = await render();
    expect(element.textContent).not.toContain('Administration');
  });

  it('ne montre pas le menu Administration à un membre', async () => {
    signIn('MEMBER');
    const element = await render();
    expect(element.textContent).not.toContain('Administration');
  });

  it("montre les liens d'administration à un admin", async () => {
    signIn('ADMIN');
    const element = await render();

    expect(element.textContent).toContain('Administration');
    expect(element.querySelector('a[href="/clubs"]')).not.toBeNull();
    expect(element.querySelector('a[href="/admin/anonymisation"]')).not.toBeNull();
    expect(element.querySelector('a[href="/admin/rgpd"]')).not.toBeNull();
    expect(element.querySelector('a[href="/admin/cgu"]')).not.toBeNull();
    expect(element.querySelector('a[href="/admin/comptes"]')).not.toBeNull();
  });

  it('ne montre pas le lien Mes évènements à un membre', async () => {
    signIn('MEMBER');
    const element = await render();
    expect(element.textContent).not.toContain('Mes évènements');
  });

  it('montre le lien Mes évènements à un organisateur', async () => {
    signIn('ORGANIZER');
    const element = await render();
    expect(element.querySelector('a[href="/mes-evenements"]')).not.toBeNull();
  });
  it('ouvre et ferme le menu mobile avec le bouton', async () => {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    const toggle = element.querySelector<HTMLButtonElement>('.header__toggle')!;
    const menu = element.querySelector('.header__menu')!;

    expect(toggle.getAttribute('aria-expanded')).toBe('false');
    expect(menu.classList.contains('header__menu--open')).toBe(false);

    toggle.click();
    await fixture.whenStable();

    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    expect(menu.classList.contains('header__menu--open')).toBe(true);

    toggle.click();
    await fixture.whenStable();

    expect(menu.classList.contains('header__menu--open')).toBe(false);
  });

  it('referme le menu mobile quand on clique sur un lien', async () => {
    const fixture = TestBed.createComponent(Header);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    const menu = element.querySelector('.header__menu')!;

    element.querySelector<HTMLButtonElement>('.header__toggle')!.click();
    await fixture.whenStable();
    element.querySelector<HTMLAnchorElement>('.header__nav a')!.click();
    await fixture.whenStable();

    expect(menu.classList.contains('header__menu--open')).toBe(false);
  });

  it('montre le lien Rechercher à tout le monde', async () => {
    const element = await render();
    expect(element.querySelector('a[href="/recherche"]')).not.toBeNull();
  });
});
