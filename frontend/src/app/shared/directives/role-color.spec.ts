import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Role } from '../../domain/role';
import { RoleColor } from './role-color';

@Component({
  imports: [RoleColor],
  template: `<span [appRoleColor]="role()">badge</span>`,
})
class Host {
  readonly role = signal<Role>('MEMBER');
}

describe('RoleColor', () => {
  it('applique la couleur du rôle et la met à jour', async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const span = (fixture.nativeElement as HTMLElement).querySelector('span')!;

    expect(span.style.getPropertyValue('--role-color')).toBe('#2563eb');
    expect(span.style.getPropertyValue('--role-bg')).toBe('rgb(37 99 235 / 12%)');
    expect(span.dataset['role']).toBe('MEMBER');

    fixture.componentInstance.role.set('ADMIN');
    await fixture.whenStable();
    expect(span.style.getPropertyValue('--role-color')).toBe('#b91c1c');
  });
});
