import { Component, input } from '@angular/core';
import { Role } from '../../../domain/role';
import { RoleColor } from '../../directives/role-color';
import { RoleLabelPipe } from '../../pipes/role-label-pipe';

/** Badge coloré affichant un rôle utilisateur. La couleur vient de RoleColor, appliquée à l'hôte. */
@Component({
  selector: 'app-role-badge',
  imports: [RoleLabelPipe],
  hostDirectives: [{ directive: RoleColor, inputs: ['appRoleColor: role'] }],
  templateUrl: './role-badge.html',
  styleUrl: './role-badge.css',
})
export class RoleBadge {
  readonly role = input.required<Role>();
}
