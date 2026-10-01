import { Directive, computed, input } from '@angular/core';
import { Category } from '../../core/events/event.model';

const CATEGORY_COLORS: Record<Category, { text: string; background: string }> = {
  CULTURE: { text: '#2892c3', background: 'rgb(40 146 195 / 12%)' },
  SPORT: { text: '#d2694a', background: 'rgb(210 105 74 / 12%)' },
  LEISURE: { text: '#2e9c86', background: 'rgb(46 156 134 / 12%)' },
};

/**
 * Directive d'attribut : applique la couleur d'une catégorie (évènement ou club) à
 * l'élément hôte, via les variables CSS --category-color et --category-bg.
 * Usage : <span [appCategoryColor]="'SPORT'">Sport</span>
 */
@Directive({
  selector: '[appCategoryColor]',
  host: {
    '[style.--category-color]': 'color().text',
    '[style.--category-bg]': 'color().background',
    '[attr.data-category]': 'appCategoryColor()',
  },
})
export class CategoryColor {
  readonly appCategoryColor = input.required<Category>();

  protected readonly color = computed(() => CATEGORY_COLORS[this.appCategoryColor()]);
}
