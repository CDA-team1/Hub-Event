import { Pipe, PipeTransform } from '@angular/core';
import { Role } from '../../domain/role';

const ROLE_LABELS: Record<Role, string> = {
  MEMBER: 'Membre',
  ORGANIZER: 'Organisateur',
  ADMIN: 'Administrateur',
};

/** Libellé lisible d'un rôle : {{ 'ADMIN' | roleLabel }} → Administrateur */
@Pipe({ name: 'roleLabel' })
export class RoleLabelPipe implements PipeTransform {
  transform(role: Role): string {
    return ROLE_LABELS[role];
  }
}
