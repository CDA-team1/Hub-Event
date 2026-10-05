import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Column } from './column';
import { DataTable, DataTableColumn } from './data-table';

interface Row {
  id: number;
  name: string;
}

@Component({
  imports: [DataTable, Column],
  template: `
    <app-data-table [columns]="columns" [rows]="rows">
      <ng-template appColumn="name" let-row>{{ row.name }}</ng-template>
      <ng-template appColumn="actions" let-row>
        <button type="button">Action {{ row.id }}</button>
      </ng-template>
    </app-data-table>
  `,
})
class Host {
  readonly columns: DataTableColumn[] = [
    { key: 'name', header: 'Nom' },
    { key: 'actions', header: 'Actions' },
  ];
  readonly rows: Row[] = [
    { id: 1, name: 'Alpha' },
    { id: 2, name: 'Bravo' },
  ];
}

describe('DataTable', () => {
  it("affiche un en-tête par colonne et projette le contenu de chaque cellule", async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    const headers = Array.from(element.querySelectorAll('th')).map((th) => th.textContent?.trim());
    expect(headers).toEqual(['Nom', 'Actions']);

    const rows = element.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(2);
    expect(rows[0].textContent).toContain('Alpha');
    expect(rows[0].querySelector('button')?.textContent).toContain('Action 1');
    expect(rows[1].textContent).toContain('Bravo');
  });
});
