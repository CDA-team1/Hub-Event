import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Column } from './column';
import { DataTable, DataTableColumn, DataTableSort } from './data-table';

interface Row {
  id: number;
  name: string;
}

@Component({
  imports: [DataTable, Column],
  template: `
    <app-data-table
      [columns]="columns"
      [rows]="rows"
      [sort]="sort()"
      (sortChanged)="onSortChanged($event)"
    >
      <ng-template appColumn="name" let-row>{{ row.name }}</ng-template>
      <ng-template appColumn="actions" let-row>
        <button type="button">Action {{ row.id }}</button>
      </ng-template>
    </app-data-table>
  `,
})
class Host {
  readonly columns: DataTableColumn[] = [
    { key: 'name', header: 'Nom', sortable: true },
    { key: 'actions', header: 'Actions' },
  ];
  readonly rows: Row[] = [
    { id: 1, name: 'Alpha' },
    { id: 2, name: 'Bravo' },
  ];
  readonly sort = signal<DataTableSort | null>(null);
  readonly sortChanged: string[] = [];

  onSortChanged(key: string): void {
    this.sortChanged.push(key);
  }
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

  it("n'affiche pas de bouton de tri sur une colonne non triable", async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    const headers = element.querySelectorAll('th');
    expect(headers[0].querySelector('button')).not.toBeNull();
    expect(headers[1].querySelector('button')).toBeNull();
  });

  it("émet la clé de la colonne au clic sur son en-tête, sans trier les lignes lui-même", async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    element.querySelector<HTMLButtonElement>('th button')!.click();

    expect(fixture.componentInstance.sortChanged).toEqual(['name']);
    const rows = element.querySelectorAll('tbody tr');
    expect(rows[0].textContent).toContain('Alpha');
  });

  it('affiche la flèche du tri courant sur la bonne colonne', async () => {
    const fixture = TestBed.createComponent(Host);
    fixture.componentInstance.sort.set({ key: 'name', direction: 'asc' });
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    const header = element.querySelector('th')!;
    expect(header.querySelector('.sort-indicator')?.textContent).toBe('▲');
    expect(header.getAttribute('aria-sort')).toBe('ascending');

    fixture.componentInstance.sort.set({ key: 'name', direction: 'desc' });
    await fixture.whenStable();
    expect(header.querySelector('.sort-indicator')?.textContent).toBe('▼');
    expect(header.getAttribute('aria-sort')).toBe('descending');
  });

  it('marque les colonnes à masquer sur petit écran', async () => {
    const fixture = TestBed.createComponent(DataTable);
    fixture.componentRef.setInput('columns', [
      { key: 'name', header: 'Nom' },
      { key: 'extra', header: 'Extra', hideOnSmall: true },
    ]);
    fixture.componentRef.setInput('rows', [{ id: 1, name: 'Alpha' }]);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    const hidden = (selector: string) =>
      Array.from(element.querySelectorAll(selector)).map((cell) =>
        cell.classList.contains('hide-on-small'),
      );

    expect(hidden('th')).toEqual([false, true]);
    expect(hidden('tbody td')).toEqual([false, true]);
  });
});
