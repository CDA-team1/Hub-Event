import { Directive, computed, input } from '@angular/core';
import { ActionLevel } from '../../domain/action-level';

const ACTION_COLORS: Record<ActionLevel, { text: string; background: string }> = {
  neutral: { text: '#64748b', background: 'rgb(100 116 139 / 12%)' },
  primary: { text: '#2563eb', background: 'rgb(37 99 235 / 12%)' },
  danger: { text: '#d33f3f', background: 'rgb(211 63 63 / 12%)' },
  success: { text: '#2e9c55', background: 'rgb(46 156 85 / 12%)' },
};

/**
 * Directive d'attribut : applique la couleur d'un niveau d'action (bouton d'action dans
 * une table dynamique) à l'élément hôte, via les variables CSS --action-color et --action-bg.
 * Usage : <span [appActionColor]="'danger'">❌</span>
 */
@Directive({
  selector: '[appActionColor]',
  host: {
    '[style.--action-color]': 'color().text',
    '[style.--action-bg]': 'color().background',
    '[attr.data-action-level]': 'appActionColor()',
  },
})
export class ActionColor {
  readonly appActionColor = input.required<ActionLevel>();

  protected readonly color = computed(() => ACTION_COLORS[this.appActionColor()]);
}
