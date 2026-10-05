import { Directive, TemplateRef, input } from '@angular/core';

/**
 * Marque un `<ng-template>` comme le contenu d'une colonne de `DataTable`, repéré par sa clé.
 * Usage : `<ng-template appColumn="category" let-row><app-category-badge [category]="row.category" /></ng-template>`
 */
@Directive({ selector: 'ng-template[appColumn]' })
export class Column<T> {
  readonly appColumn = input.required<string>();

  constructor(readonly templateRef: TemplateRef<{ $implicit: T }>) {}
}
