import { Category } from './category';

const DEFAULT_IMAGES: Record<Category, string> = {
  CULTURE: 'placeholders/culture.webp',
  LEISURE: 'placeholders/leisure.webp',
  SPORT: 'placeholders/sport.webp',
};

/** Illustration affichée à la place d'une photo, tant qu'un évènement n'en a aucune. */
export function defaultEventImage(category: Category): string {
  return DEFAULT_IMAGES[category];
}
