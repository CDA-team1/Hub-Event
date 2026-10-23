import { Pipe, PipeTransform } from '@angular/core';
import { Category } from '../../domain/category';

const CATEGORY_LABELS: Record<Category, string> = {
  CULTURE: 'Culture',
  SPORT: 'Sport',
  LEISURE: 'Loisirs',
};

/** Libellé lisible d'une catégorie : {{ 'LEISURE' | categoryLabel }} → Loisirs */
@Pipe({ name: 'categoryLabel' })
export class CategoryLabelPipe implements PipeTransform {
  transform(category: Category): string {
    return CATEGORY_LABELS[category];
  }
}
