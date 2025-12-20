import { Component, Input, Output, EventEmitter, TemplateRef } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface TableColumn {
  key: string;
  label: string;
  sortable?: boolean;
  template?: TemplateRef<any>;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="data-table-container">
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th *ngFor="let column of columns" [class.sortable]="column.sortable">
                {{ column.label }}
                <span *ngIf="column.sortable && sortColumn === column.key" class="sort-icon">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th *ngIf="actionsTemplate">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let row of data; let i = index" [class.even]="i % 2 === 0">
              <td *ngFor="let column of columns">
                <ng-container *ngIf="column.template; else defaultCell">
                  <ng-container *ngTemplateOutlet="column.template; context: { $implicit: row, column: column }"></ng-container>
                </ng-container>
                <ng-template #defaultCell>
                  {{ getCellValue(row, column.key) }}
                </ng-template>
              </td>
              <td *ngIf="actionsTemplate" class="actions-cell">
                <ng-container *ngTemplateOutlet="actionsTemplate; context: { $implicit: row }"></ng-container>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div *ngIf="data.length === 0" class="empty-state">
        <p>No data available</p>
      </div>
    </div>
  `,
  styles: [`
    .data-table-container {
      background: white;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .table-wrapper {
      overflow-x: auto;
    }

    .data-table {
      width: 100%;
      border-collapse: collapse;
    }

    thead {
      background-color: #f8f9fa;
    }

    th {
      padding: 12px;
      text-align: left;
      font-weight: 600;
      border-bottom: 2px solid #dee2e6;
    }

    th.sortable {
      cursor: pointer;
      user-select: none;
    }

    th.sortable:hover {
      background-color: #e9ecef;
    }

    .sort-icon {
      margin-left: 5px;
      font-size: 12px;
    }

    tbody tr {
      border-bottom: 1px solid #dee2e6;
    }

    tbody tr:hover {
      background-color: #f8f9fa;
    }

    tbody tr.even {
      background-color: #ffffff;
    }

    td {
      padding: 12px;
    }

    .actions-cell {
      white-space: nowrap;
    }

    .empty-state {
      padding: 40px;
      text-align: center;
      color: #6c757d;
    }
  `]
})
export class DataTableComponent {
  @Input() data: any[] = [];
  @Input() columns: TableColumn[] = [];
  @Input() actionsTemplate?: TemplateRef<any>;
  @Output() sortChange = new EventEmitter<{ column: string; direction: 'asc' | 'desc' }>();

  sortColumn: string | null = null;
  sortDirection: 'asc' | 'desc' = 'asc';

  getCellValue(row: any, key: string): any {
    return key.split('.').reduce((obj, k) => obj?.[k], row) ?? '-';
  }

  onSort(column: TableColumn): void {
    if (!column.sortable) return;

    if (this.sortColumn === column.key) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column.key;
      this.sortDirection = 'asc';
    }

    this.sortChange.emit({ column: column.key, direction: this.sortDirection });
  }
}

