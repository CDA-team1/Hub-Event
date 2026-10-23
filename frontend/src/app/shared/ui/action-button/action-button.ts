import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ActionLevel } from '../../../domain/action-level';
import { ActionColor } from '../../directives/action-color';

/**
 * Bouton d'action icône-seule pour les colonnes "Actions" des tables dynamiques.
 * Avec [routerLink], navigue (ex. "Voir détails", "Modifier). Sans, émet (triggered)
 * au clic (ex. "Se désinscrire", "Supprimer"). La couleur vient de ActionColor, appliquée à l'hôte.
 */
@Component({
  selector: 'app-action-button',
  imports: [RouterLink],
  hostDirectives: [{ directive: ActionColor, inputs: ['appActionColor: level'] }],
  templateUrl: './action-button.html',
  styleUrl: './action-button.css',
})
export class ActionButton {
  readonly icon = input.required<string>();
  readonly label = input.required<string>();
  readonly level = input.required<ActionLevel>();
  readonly routerLink = input<string | readonly unknown[]>();

  readonly triggered = output<void>();
}
