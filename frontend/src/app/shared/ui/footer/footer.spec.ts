import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Footer } from './footer';

describe('Footer', () => {
  let component: Footer;
  let fixture: ComponentFixture<Footer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Footer],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Footer);
    component = fixture.componentInstance;
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('affiche un lien RGPD, avec la politique complète en infobulle', () => {
    const link = fixture.nativeElement.querySelector('a[href="/rgpd"]');

    expect(link.textContent.trim()).toBe('RGPD');
    expect(link.getAttribute('title')).toBe('Politique RGPD');
  });

  it('affiche un lien CGU, avec les conditions complètes en infobulle', () => {
    const link = fixture.nativeElement.querySelector('a[href="/cgu"]');

    expect(link.textContent.trim()).toBe('CGU');
    expect(link.getAttribute('title')).toBe('Conditions Générales d’Utilisation');
  });
});
