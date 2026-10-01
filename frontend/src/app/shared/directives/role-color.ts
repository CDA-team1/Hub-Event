import { Directive, computed, input } from '@angular/core';
import { Role } from '../../domain/role';

const ROLE_COLORS: Record<Role, { text: string; background: string }> = {
  MEMBER: { text: '#2563eb', background: 'rgb(37 99 235 / 12%)' },
  ORGANIZER: { text: '#7c3aed', background: 'rgb(124 58 237 / 12%)' },
  ADMIN: { text: '#b91c1c', background: 'rgb(185 28 28 / 12%)' },
};

/**
 * Directive d'attribut : applique la couleur d'un rôle utilisateur à l'élément hôte,
 * via les variables CSS --role-color et --role-bg.
 * Usage : <span [appRoleColor]="'ADMIN'">Administrateur</span>
 */
@Directive({
  selector: '[appRoleColor]',
  host: {
    '[style.--role-color]': 'color().text',
    '[style.--role-bg]': 'color().background',
    '[attr.data-role]': 'appRoleColor()',
  },
})
export class RoleColor {
  readonly appRoleColor = input.required<Role>();

  protected readonly color = computed(() => ROLE_COLORS[this.appRoleColor()]);
}
