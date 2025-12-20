import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificationRequestService } from '../../../core/services/notificationrequest.service';
import { CompanyService } from '../../../core/services/company.service';
import { NotificationRequest, NotificationRequestRequest } from '../../../core/models/notificationrequest.model';
import { CompanyResponse } from '../../../core/models/company.model';

@Component({
  selector: 'app-notification-requests',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="notification-requests-container">
      <div class="header">
        <h1>Notification Requests Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Request</button>
      </div>

      <div class="filters">
        <label>Filter by Company:</label>
        <select [(ngModel)]="selectedCompanyKey" (change)="loadRequests()">
          <option [value]="null">All Companies</option>
          <option *ngFor="let company of companies" [value]="company.companyKey">
            {{ company.name }}
          </option>
        </select>
        <label>Filter by Status:</label>
        <select [(ngModel)]="selectedStatus" (change)="loadRequests()">
          <option value="">All Statuses</option>
          <option value="pending">Pending</option>
          <option value="processing">Processing</option>
          <option value="completed">Completed</option>
          <option value="failed">Failed</option>
        </select>
        <label>Filter by Target Type:</label>
        <select [(ngModel)]="selectedTargetType" (change)="loadRequests()">
          <option value="">All Types</option>
          <option value="user">User</option>
          <option value="company">Company</option>
          <option value="role">Role</option>
          <option value="all">All</option>
        </select>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading" class="loading">Loading notification requests...</div>

      <div class="requests-list" *ngIf="requests.length > 0">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Company</th>
              <th>Type</th>
              <th>Target</th>
              <th>Status</th>
              <th>Scheduled</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let request of requests">
              <td>{{ request.title }}</td>
              <td>{{ request.companyName || '-' }}</td>
              <td>{{ request.type || '-' }}</td>
              <td>{{ request.targetType || '-' }}</td>
              <td>
                <span [class]="'badge status-' + request.status">
                  {{ request.status }}
                </span>
              </td>
              <td>{{ request.scheduledFor | date:'short' || '-' }}</td>
              <td>{{ request.createdAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="viewRequest(request)">View</button>
                <button class="btn btn-sm btn-danger" (click)="deleteRequest(request)">Delete</button>
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
    </div>

    <div *ngIf="selectedRequest" class="modal-overlay" (click)="closeModal()">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h2>Notification Request Details</h2>
          <button class="btn-close" (click)="closeModal()">×</button>
        </div>
        <div class="modal-body">
          <p><strong>Title:</strong> {{ selectedRequest.title }}</p>
          <p><strong>Message:</strong> {{ selectedRequest.message }}</p>
          <p><strong>Company:</strong> {{ selectedRequest.companyName }}</p>
          <p><strong>Type:</strong> {{ selectedRequest.type || '-' }}</p>
          <p><strong>Target Type:</strong> {{ selectedRequest.targetType || '-' }}</p>
          <p><strong>Status:</strong> {{ selectedRequest.status }}</p>
          <p *ngIf="selectedRequest.scheduledFor"><strong>Scheduled For:</strong> {{ selectedRequest.scheduledFor | date:'full' }}</p>
          <p *ngIf="selectedRequest.processedAt"><strong>Processed At:</strong> {{ selectedRequest.processedAt | date:'full' }}</p>
          <p *ngIf="selectedRequest.actionUrl"><strong>Action URL:</strong> {{ selectedRequest.actionUrl }}</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .notification-requests-container {
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
      flex-wrap: wrap;
    }

    .filters label {
      font-weight: 500;
    }

    .filters select {
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .requests-list {
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

    .badge.status-processing {
      background-color: #17a2b8;
      color: white;
    }

    .badge.status-completed {
      background-color: #28a745;
      color: white;
    }

    .badge.status-failed {
      background-color: #dc3545;
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
export class NotificationRequestsComponent implements OnInit {
  requests: NotificationRequest[] = [];
  companies: CompanyResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  currentPage = 0;
  totalPages = 0;
  selectedCompanyKey: string | null = null;
  selectedStatus: string = '';
  selectedTargetType: string = '';
  selectedRequest: NotificationRequest | null = null;

  constructor(
    private notificationRequestService: NotificationRequestService,
    private companyService: CompanyService
  ) {}

  ngOnInit(): void {
    this.loadCompanies();
    this.loadRequests();
  }

  loadCompanies(): void {
    this.companyService.getAllCompanies().subscribe({
      next: (response) => {
        this.companies = response.companies || [];
      }
    });
  }

  loadRequests(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.notificationRequestService.listRequests(
      page, 
      20, 
      this.selectedCompanyKey || undefined, 
      this.selectedStatus || undefined,
      this.selectedTargetType || undefined
    ).subscribe({
      next: (response) => {
        this.requests = response.notificationRequests || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load notification requests';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadRequests(page);
  }

  viewRequest(request: NotificationRequest): void {
    this.selectedRequest = request;
  }

  closeModal(): void {
    this.selectedRequest = null;
  }

  deleteRequest(request: NotificationRequest): void {
    if (confirm(`Delete notification request "${request.title}"?`)) {
      this.loading = true;
      this.notificationRequestService.deleteRequest(request.requestKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadRequests(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete notification request';
          this.loading = false;
        }
      });
    }
  }
}

