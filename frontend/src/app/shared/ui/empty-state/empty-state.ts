import { Component } from '@angular/core';

/**
 * Bloc « état vide » : le contenu est projeté par le parent.
 * Le slot [actions] reçoit les boutons ou liens, masqué s'il est vide.
 */
@Component({
  selector: 'app-empty-state',
  templateUrl: './empty-state.html',
  styleUrl: './empty-state.css',
})
export class EmptyState {}
