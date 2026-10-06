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

  it("ne montre pas le menu Administration à un visiteur non connecté", async () => {
    const element = await render();
    expect(element.textContent).not.toContain('Administration');
  });

  it("ne montre pas le menu Administration à un membre", async () => {
    signIn('MEMBER');
    const element = await render();
    expect(element.textContent).not.toContain('Administration');
  });

  it("montre le menu Administration avec le lien Clubs à un admin", async () => {
    signIn('ADMIN');
    const element = await render();
    expect(element.textContent).toContain('Administration');
    expect(element.querySelector('a[href="/clubs"]')).not.toBeNull();
  });

  it("ne montre pas le lien Mes évènements à un membre", async () => {
    signIn('MEMBER');
    const element = await render();
    expect(element.textContent).not.toContain('Mes évènements');
  });

  it("montre le lien Mes évènements à un organisateur", async () => {
    signIn('ORGANIZER');
    const element = await render();
    expect(element.querySelector('a[href="/mes-evenements"]')).not.toBeNull();
  });
});
