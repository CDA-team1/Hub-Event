import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ActionButton } from './action-button';

describe('ActionButton', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('affiche un lien quand routerLink est fourni, et navigue sans émettre triggered', async () => {
    const fixture = TestBed.createComponent(ActionButton);
    fixture.componentRef.setInput('icon', '👁️');
    fixture.componentRef.setInput('label', 'Voir détails');
    fixture.componentRef.setInput('level', 'neutral');
    fixture.componentRef.setInput('routerLink', ['/evenements', 1]);
    const emitted: void[] = [];
    fixture.componentInstance.triggered.subscribe(() => emitted.push(undefined));
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    const link = element.querySelector('a')!;
    expect(link.getAttribute('aria-label')).toBe('Voir détails');
    expect(element.querySelector('button')).toBeNull();
    expect(emitted).toHaveLength(0);
  });

  it('affiche un bouton et émet triggered au clic quand aucun routerLink', async () => {
    const fixture = TestBed.createComponent(ActionButton);
    fixture.componentRef.setInput('icon', '❌');
    fixture.componentRef.setInput('label', 'Se désinscrire');
    fixture.componentRef.setInput('level', 'danger');
    const emitted: void[] = [];
    fixture.componentInstance.triggered.subscribe(() => emitted.push(undefined));
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    const button = element.querySelector('button')!;
    expect(button.getAttribute('aria-label')).toBe('Se désinscrire');
    button.click();

    expect(emitted).toHaveLength(1);
  });

  it("applique la couleur du niveau d'action à l'hôte", async () => {
    const fixture = TestBed.createComponent(ActionButton);
    fixture.componentRef.setInput('icon', '✏️');
    fixture.componentRef.setInput('label', 'Modifier');
    fixture.componentRef.setInput('level', 'primary');
    await fixture.whenStable();

    const host = fixture.nativeElement as HTMLElement;
    expect(host.style.getPropertyValue('--action-color')).toBe('#2563eb');
    expect(host.style.getPropertyValue('--action-bg')).toBe('rgb(37 99 235 / 12%)');
  });
});
