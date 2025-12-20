import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WebSubscriberService } from '../../../core/services/websubscriber.service';
import { WebSubscriberResponse } from '../../../core/models/websubscriber.model';

@Component({
  selector: 'app-subscribers',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="subscribers-container">
      <div class="header">
        <h1>Subscribers Management</h1>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading" class="loading">Loading subscribers...</div>

      <div class="subscribers-list" *ngIf="subscribers.length > 0">
        <table>
          <thead>
            <tr>
              <th>Email</th>
              <th>Name</th>
              <th>Status</th>
              <th>Subscribed</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let subscriber of subscribers">
              <td>{{ subscriber.email }}</td>
              <td>{{ subscriber.name || '-' }}</td>
              <td>
                <span [class]="subscriber.verified ? 'badge verified' : 'badge unverified'">
                  {{ subscriber.verified ? 'Verified' : 'Unverified' }}
                </span>
              </td>
              <td>{{ subscriber.createdAt | date:'short' }}</td>
              <td>
                <button *ngIf="!subscriber.verified" 
                        class="btn btn-sm btn-primary" 
                        (click)="sendVerification(subscriber)">
                  Send Verification
                </button>
                <button class="btn btn-sm btn-danger" (click)="deleteSubscriber(subscriber)">Delete</button>
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
  `,
  styles: [`
    .subscribers-container {
      padding: 20px;
    }

    .header {
      margin-bottom: 20px;
    }

    .subscribers-list {
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
    }

    .badge.verified {
      background-color: #28a745;
      color: white;
    }

    .badge.unverified {
      background-color: #ffc107;
      color: #333;
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
  `]
})
export class SubscribersComponent implements OnInit {
  subscribers: WebSubscriberResponse[] = [];
  loading = false;
  error = '';
  currentPage = 0;
  totalPages = 0;

  constructor(private webSubscriberService: WebSubscriberService) {}

  ngOnInit(): void {
    this.loadSubscribers();
  }

  loadSubscribers(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.webSubscriberService.listSubscribers(page, 20).subscribe({
      next: (response) => {
        this.subscribers = response.subscribers || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load subscribers';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadSubscribers(page);
  }

  sendVerification(subscriber: WebSubscriberResponse): void {
    this.loading = true;
    this.webSubscriberService.sendVerificationEmail(subscriber.subscriberKey).subscribe({
      next: () => {
        this.loading = false;
        alert('Verification email sent successfully');
      },
      error: () => {
        this.error = 'Failed to send verification email';
        this.loading = false;
      }
    });
  }

  deleteSubscriber(subscriber: WebSubscriberResponse): void {
    if (confirm(`Delete subscriber ${subscriber.email}?`)) {
      this.loading = true;
      this.webSubscriberService.deleteSubscriber(subscriber.subscriberKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadSubscribers(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete subscriber';
          this.loading = false;
        }
      });
    }
  }
}

