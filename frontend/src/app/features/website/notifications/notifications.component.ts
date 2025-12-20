import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification } from '../../../core/models/notification.model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="notifications-container">
      <div class="header">
        <h1>Notifications</h1>
        <button class="btn btn-primary" (click)="markAllAsRead()" *ngIf="unreadCount > 0">
          Mark All as Read
        </button>
      </div>

      <div *ngIf="loading" class="loading">Loading notifications...</div>
      <div *ngIf="error" class="error-message">{{ error }}</div>

      <div *ngIf="notifications.length === 0 && !loading" class="empty-state">
        <p>No notifications</p>
      </div>

      <div class="notifications-list" *ngIf="notifications.length > 0">
        <div *ngFor="let notification of notifications" 
             class="notification-item" 
             [class.unread]="!notification.read"
             (click)="markAsRead(notification)">
          <div class="notification-content">
            <h3>{{ notification.title }}</h3>
            <p>{{ notification.message }}</p>
            <div class="notification-meta">
              <span class="type" [class]="'type-' + (notification.type || 'info')">
                {{ notification.type || 'info' }}
              </span>
              <span class="date">{{ notification.createdAt | date:'short' }}</span>
            </div>
            <a *ngIf="notification.actionUrl" 
               [href]="notification.actionUrl" 
               class="action-link"
               (click)="$event.stopPropagation()">
              {{ notification.actionText || 'View' }}
            </a>
          </div>
          <button class="btn-delete" (click)="deleteNotification(notification); $event.stopPropagation()">
            ×
          </button>
        </div>
      </div>

      <div class="pagination" *ngIf="totalPages > 1">
        <button (click)="loadPage(currentPage - 1)" [disabled]="currentPage === 0">Previous</button>
        <span>Page {{ currentPage + 1 }} of {{ totalPages }}</span>
        <button (click)="loadPage(currentPage + 1)" [disabled]="currentPage >= totalPages - 1">Next</button>
      </div>
    </div>
  `,
  styles: [`
    .notifications-container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .notifications-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .notification-item {
      background: white;
      padding: 15px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      cursor: pointer;
      transition: background-color 0.2s;
      border-left: 4px solid transparent;
    }

    .notification-item.unread {
      border-left-color: #007bff;
      background-color: #f0f8ff;
    }

    .notification-item:hover {
      background-color: #f8f9fa;
    }

    .notification-content {
      flex: 1;
    }

    .notification-content h3 {
      margin: 0 0 8px 0;
      font-size: 16px;
    }

    .notification-content p {
      margin: 0 0 10px 0;
      color: #666;
    }

    .notification-meta {
      display: flex;
      gap: 15px;
      align-items: center;
      margin-bottom: 10px;
    }

    .type {
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;
      text-transform: uppercase;
    }

    .type-info {
      background-color: #17a2b8;
      color: white;
    }

    .type-success {
      background-color: #28a745;
      color: white;
    }

    .type-warning {
      background-color: #ffc107;
      color: #333;
    }

    .type-error {
      background-color: #dc3545;
      color: white;
    }

    .date {
      font-size: 12px;
      color: #999;
    }

    .action-link {
      color: #007bff;
      text-decoration: none;
      font-size: 14px;
    }

    .action-link:hover {
      text-decoration: underline;
    }

    .btn-delete {
      background: none;
      border: none;
      font-size: 24px;
      color: #999;
      cursor: pointer;
      padding: 0 10px;
    }

    .btn-delete:hover {
      color: #dc3545;
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

    .error-message {
      background-color: #f8d7da;
      color: #721c24;
      padding: 12px;
      border-radius: 4px;
      margin-bottom: 20px;
    }

    .loading, .empty-state {
      text-align: center;
      padding: 40px;
      color: #999;
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
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  loading = false;
  error = '';
  currentPage = 0;
  totalPages = 0;
  unreadCount = 0;

  constructor(private notificationService: NotificationService) {
    this.notificationService.unreadCount$.subscribe(count => {
      this.unreadCount = count;
    });
  }

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.notificationService.listNotifications(page, 20).subscribe({
      next: (response) => {
        this.notifications = response.notifications || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.unreadCount = response.unreadCount || 0;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load notifications';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadNotifications(page);
  }

  markAsRead(notification: Notification): void {
    if (!notification.read) {
      this.notificationService.markAsRead(notification.notificationKey).subscribe({
        next: () => {
          notification.read = true;
        },
        error: () => {
          this.error = 'Failed to mark notification as read';
        }
      });
    }
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
      },
      error: () => {
        this.error = 'Failed to mark all as read';
      }
    });
  }

  deleteNotification(notification: Notification): void {
    if (confirm('Delete this notification?')) {
      this.notificationService.deleteNotification(notification.notificationKey).subscribe({
        next: () => {
          this.notifications = this.notifications.filter(n => n.notificationKey !== notification.notificationKey);
        },
        error: () => {
          this.error = 'Failed to delete notification';
        }
      });
    }
  }
}

