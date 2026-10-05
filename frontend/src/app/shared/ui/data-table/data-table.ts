import { NgTemplateOutlet } from '@angular/common';
import { Component, contentChildren, input } from '@angular/core';
import { Column } from './column';

export interface DataTableColumn {
  readonly key: string;
  readonly header: string;
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
 */
@Component({
  selector: 'app-data-table',
  imports: [NgTemplateOutlet],
  templateUrl: './data-table.html',
  styleUrl: './data-table.css',
})
export class DataTable<T> {
  readonly columns = input.required<DataTableColumn[]>();
  readonly rows = input.required<readonly T[]>();
  readonly trackBy = input<(row: T, index: number) => unknown>((_row, index) => index);

  private readonly cellTemplates = contentChildren(Column);

  protected templateFor(key: string) {
    return this.cellTemplates().find((column) => column.appColumn() === key)?.templateRef;
  }
}
