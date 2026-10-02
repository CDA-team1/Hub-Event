import { Component } from '@angular/core';

/**
 * Bloc « erreur de chargement » : le contenu est projeté par le parent.
 * Le slot [actions] reçoit les boutons ou liens (ex. « Réessayer »), masqué s'il est vide.
 */
@Component({
  selector: 'app-error-state',
  templateUrl: './error-state.html',
  styleUrl: './error-state.css',
})
export class ErrorState {}
