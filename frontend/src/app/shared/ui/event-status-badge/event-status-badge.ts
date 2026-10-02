import { Component, input } from '@angular/core';
import { EventStatus } from '../../../domain/event.model';
import { EventStatusColor } from '../../directives/event-status-color';
import { EventStatusLabelPipe } from '../../pipes/event-status-label-pipe';

/** Badge coloré affichant un statut d'évènement. La couleur vient de EventStatusColor, appliquée à l'hôte. */
@Component({
  selector: 'app-event-status-badge',
  imports: [EventStatusLabelPipe],
  hostDirectives: [{ directive: EventStatusColor, inputs: ['appEventStatusColor: status'] }],
  templateUrl: './event-status-badge.html',
  styleUrl: './event-status-badge.css',
})
export class EventStatusBadge {
  readonly status = input.required<EventStatus>();
}
