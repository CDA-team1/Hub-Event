import { Pipe, PipeTransform } from '@angular/core';
import { EventStatus } from '../../domain/event.model';

const EVENT_STATUS_LABELS: Record<EventStatus, string> = {
  DRAFT: 'Brouillon',
  PUBLISHED: 'Publié',
  CANCELLED: 'Annulé',
  FINISHED: 'Terminé',
};

/** Libellé lisible d'un statut d'évènement : {{ 'PUBLISHED' | eventStatusLabel }} → Publié */
@Pipe({ name: 'eventStatusLabel' })
export class EventStatusLabelPipe implements PipeTransform {
  transform(status: EventStatus): string {
    return EVENT_STATUS_LABELS[status];
  }
}
