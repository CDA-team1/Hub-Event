import { EventStatusLabelPipe } from './event-status-label-pipe';

describe('EventStatusLabelPipe', () => {
  it.each([
    ['DRAFT', 'Brouillon'],
    ['PUBLISHED', 'Publié'],
    ['CANCELLED', 'Annulé'],
    ['FINISHED', 'Terminé'],
  ] as const)('%s → %s', (status, expected) => {
    expect(new EventStatusLabelPipe().transform(status)).toBe(expected);
  });
});
