import { NgTemplateOutlet } from '@angular/common';
import { Component, contentChildren, input, output } from '@angular/core';
import { Column } from './column';

export interface DataTableColumn {
  readonly key: string;
  readonly header: string;
  /** Rend l'en-tête cliquable ; le tri lui-même reste à la charge de l'appelant (voir `sort`). */
  readonly sortable?: boolean;
  /** Masquée sur petit écran (640 px et moins) : l'appelant reprend son contenu ailleurs. */
  readonly hideOnSmall?: boolean;
}

export interface DataTableSort {
  readonly key: string;
  readonly direction: 'asc' | 'desc';
}

/**
 * Tableau générique : colonnes et contenu des cellules définis par l'appelant via `appColumn`,
 * la liste des colonnes ne fixe que leur ordre et leur en-tête.
 * Usage :
 * ```html
 * <app-data-table [columns]="columns" [rows]="clubs()">
 *   <ng-template appColumn="name" let-club>{{ club.name }}</ng-template>
 *   <ng-template appColumn="category" let-club><app-category-badge [category]="club.category" /></ng-template>
 * </app-data-table>
 * ```
 * Tri (colonnes avec `sortable: true`) : le tableau affiche l'en-tête cliquable et la flèche
 * du tri courant, mais ne trie jamais `rows` lui-même — il ne connaît pas la sémantique de `T`
 * (ex. trier un statut par ordre alphabétique n'aurait pas de sens). C'est l'appelant qui
 * possède l'état du tri et réordonne ses données en réponse à `(sortChanged)`.
 */
@Component({
  selector: 'app-data-table',
  imports: [NgTemplateOutlet],
  templateUrl: './data-table.html',
  styleUrl: './data-table.css',
  host: { '[class.bordered]': 'bordered()' },
})
export class DataTable<T> {
  readonly columns = input.required<DataTableColumn[]>();
  readonly rows = input.required<readonly T[]>();
  readonly trackBy = input<(row: T, index: number) => unknown>((_row, index) => index);
  readonly sort = input<DataTableSort | null>(null);
  /** Grille complète : cadre arrondi, en-tête grisé et traits entre les cellules. */
  readonly bordered = input(false);

  readonly sortChanged = output<string>();

  private readonly cellTemplates = contentChildren(Column);

  protected templateFor(key: string) {
    return this.cellTemplates().find((column) => column.appColumn() === key)?.templateRef;
  }

  protected onHeaderClick(column: DataTableColumn): void {
    this.sortChanged.emit(column.key);
  }

  protected sortDirection(column: DataTableColumn): 'asc' | 'desc' | null {
    const sort = this.sort();
    return sort?.key === column.key ? sort.direction : null;
  }

  protected ariaSort(column: DataTableColumn): 'ascending' | 'descending' | 'none' {
    switch (this.sortDirection(column)) {
      case 'asc':
        return 'ascending';
      case 'desc':
        return 'descending';
      default:
        return 'none';
    }
  }
}
