import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../../core/services/invoice.service';
import { CompanyService } from '../../../core/services/company.service';
import { CompanyInvoice, CompanyInvoiceRequest } from '../../../core/models/invoice.model';
import { CompanyResponse } from '../../../core/models/company.model';

@Component({
  selector: 'app-invoices',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="invoices-container">
      <div class="header">
        <h1>Invoices Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Invoice</button>
      </div>

      <div class="filters">
        <label>Filter by Company:</label>
        <select [(ngModel)]="selectedCompanyKey" (change)="loadInvoices()">
          <option [value]="null">All Companies</option>
          <option *ngFor="let company of companies" [value]="company.companyKey">
            {{ company.name }}
          </option>
        </select>
        <label>Filter by Status:</label>
        <select [(ngModel)]="selectedStatus" (change)="loadInvoices()">
          <option value="">All Statuses</option>
          <option value="pending">Pending</option>
          <option value="paid">Paid</option>
          <option value="overdue">Overdue</option>
          <option value="cancelled">Cancelled</option>
        </select>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading" class="loading">Loading invoices...</div>

      <div class="invoices-list" *ngIf="invoices.length > 0">
        <table>
          <thead>
            <tr>
              <th>Invoice #</th>
              <th>Company</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Due Date</th>
              <th>Paid Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let invoice of invoices">
              <td>{{ invoice.invoiceNumber }}</td>
              <td>{{ invoice.companyName || '-' }}</td>
              <td>{{ invoice.amount | number:'1.2-2' }} {{ invoice.currency }}</td>
              <td>
                <span [class]="'badge status-' + invoice.status">
                  {{ invoice.status }}
                </span>
              </td>
              <td>{{ invoice.dueDate | date:'short' }}</td>
              <td>{{ invoice.paidDate | date:'short' || '-' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="viewInvoice(invoice)">View</button>
                <button class="btn btn-sm btn-danger" (click)="deleteInvoice(invoice)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="pagination" *ngIf="totalPages > 1">
          <button (click)="loadPage(currentPage - 1)" [disabled]="currentPage === 0">Previous</button>
          <span>Page {{ currentPage + 1 }} of {{ totalPages }}</span>
          <button (click)="loadPage(currentPage + 1)" [disabled]="currentPage >= totalPages - 1">Next</button>
        </div>
      </div>

      <div *ngIf="selectedInvoice" class="modal-overlay" (click)="closeModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Invoice Details</h2>
            <button class="btn-close" (click)="closeModal()">×</button>
          </div>
          <div class="modal-body">
            <p><strong>Invoice Number:</strong> {{ selectedInvoice.invoiceNumber }}</p>
            <p><strong>Company:</strong> {{ selectedInvoice.companyName }}</p>
            <p><strong>Amount:</strong> {{ selectedInvoice.amount | number:'1.2-2' }} {{ selectedInvoice.currency }}</p>
            <p><strong>Status:</strong> {{ selectedInvoice.status }}</p>
            <p><strong>Due Date:</strong> {{ selectedInvoice.dueDate | date:'full' }}</p>
            <p *ngIf="selectedInvoice.paidDate"><strong>Paid Date:</strong> {{ selectedInvoice.paidDate | date:'full' }}</p>
            <p *ngIf="selectedInvoice.description"><strong>Description:</strong> {{ selectedInvoice.description }}</p>
            <p *ngIf="selectedInvoice.notes"><strong>Notes:</strong> {{ selectedInvoice.notes }}</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .invoices-container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .filters {
      display: flex;
      gap: 15px;
      margin-bottom: 20px;
      padding: 15px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      align-items: center;
    }

    .filters label {
      font-weight: 500;
    }

    .filters select {
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .invoices-list {
      background: white;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    table {
      width: 100%;
      border-collapse: collapse;
    }

    th, td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }

    th {
      background-color: #f8f9fa;
      font-weight: 600;
    }

    .badge {
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 12px;
      text-transform: capitalize;
    }

    .badge.status-pending {
      background-color: #ffc107;
      color: #333;
    }

    .badge.status-paid {
      background-color: #28a745;
      color: white;
    }

    .badge.status-overdue {
      background-color: #dc3545;
      color: white;
    }

    .badge.status-cancelled {
      background-color: #6c757d;
      color: white;
    }

    .btn {
      padding: 8px 16px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
    }

    .btn-primary {
      background-color: #007bff;
      color: white;
    }

    .btn-danger {
      background-color: #dc3545;
      color: white;
    }

    .btn-sm {
      padding: 4px 8px;
      font-size: 12px;
      margin-right: 5px;
    }

    .error-message {
      background-color: #f8d7da;
      color: #721c24;
      padding: 12px;
      border-radius: 4px;
      margin-bottom: 20px;
    }

    .loading {
      text-align: center;
      padding: 20px;
    }

    .pagination {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 20px;
      padding: 20px;
    }

    .pagination button {
      padding: 8px 16px;
      border: 1px solid #ddd;
      background: white;
      border-radius: 4px;
      cursor: pointer;
    }

    .pagination button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .modal-content {
      background: white;
      border-radius: 8px;
      max-width: 600px;
      width: 90%;
      max-height: 80vh;
      overflow-y: auto;
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #ddd;
    }

    .btn-close {
      background: none;
      border: none;
      font-size: 24px;
      cursor: pointer;
      color: #999;
    }

    .modal-body {
      padding: 20px;
    }

    .modal-body p {
      margin: 10px 0;
    }
  `]
})
export class InvoicesComponent implements OnInit {
  invoices: CompanyInvoice[] = [];
  companies: CompanyResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  currentPage = 0;
  totalPages = 0;
  selectedCompanyKey: string | null = null;
  selectedStatus: string = '';
  selectedInvoice: CompanyInvoice | null = null;

  constructor(
    private invoiceService: InvoiceService,
    private companyService: CompanyService
  ) {}

  ngOnInit(): void {
    this.loadCompanies();
    this.loadInvoices();
  }

  loadCompanies(): void {
    this.companyService.getAllCompanies().subscribe({
      next: (response) => {
        this.companies = response.companies || [];
      }
    });
  }

  loadInvoices(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.invoiceService.listInvoices(page, 20, this.selectedCompanyKey || undefined, this.selectedStatus || undefined).subscribe({
      next: (response) => {
        this.invoices = response.invoices || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load invoices';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadInvoices(page);
  }

  viewInvoice(invoice: CompanyInvoice): void {
    this.selectedInvoice = invoice;
  }

  closeModal(): void {
    this.selectedInvoice = null;
  }

  deleteInvoice(invoice: CompanyInvoice): void {
    if (confirm(`Delete invoice ${invoice.invoiceNumber}?`)) {
      this.loading = true;
      this.invoiceService.deleteInvoice(invoice.invoiceKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadInvoices(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete invoice';
          this.loading = false;
        }
      });
    }
  }
}

