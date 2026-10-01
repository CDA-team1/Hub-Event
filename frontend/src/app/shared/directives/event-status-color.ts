import { Directive, computed, input } from '@angular/core';
import { EventStatus } from '../../core/events/event.model';

const EVENT_STATUS_COLORS: Record<EventStatus, { text: string; background: string }> = {
  DRAFT: { text: '#6b7280', background: 'rgb(107 114 128 / 12%)' },
  PUBLISHED: { text: '#2e9c55', background: 'rgb(46 156 85 / 12%)' },
  CANCELLED: { text: '#d33f3f', background: 'rgb(211 63 63 / 12%)' },
  FINISHED: { text: '#6366a8', background: 'rgb(99 102 168 / 12%)' },
};

/**
 * Directive d'attribut : applique la couleur d'un statut d'évènement à l'élément hôte,
 * via les variables CSS --status-color et --status-bg.
 * Usage : <span [appEventStatusColor]="'PUBLISHED'">Publié</span>
 */
@Directive({
  selector: '[appEventStatusColor]',
  host: {
    '[style.--status-color]': 'color().text',
    '[style.--status-bg]': 'color().background',
    '[attr.data-status]': 'appEventStatusColor()',
  },
})
export class EventStatusColor {
  readonly appEventStatusColor = input.required<EventStatus>();

  protected readonly color = computed(() => EVENT_STATUS_COLORS[this.appEventStatusColor()]);
}
