import { Component, input } from '@angular/core';
import { Category } from '../../../core/events/event.model';
import { CategoryColor } from '../../directives/category-color';
import { CategoryLabelPipe } from '../../pipes/category-label-pipe';

/** Badge coloré affichant une catégorie. La couleur vient de CategoryColor, appliquée à l'hôte. */
@Component({
  selector: 'app-category-badge',
  imports: [CategoryLabelPipe],
  hostDirectives: [{ directive: CategoryColor, inputs: ['appCategoryColor: category'] }],
  templateUrl: './category-badge.html',
  styleUrl: './category-badge.css',
})
export class CategoryBadge {
  readonly category = input.required<Category>();
}
