import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-website-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="layout-container">
      <nav class="sidebar">
        <div class="sidebar-header">
          <h3>Website Management</h3>
        </div>
        <ul class="nav-menu">
          <li>
            <a routerLink="/website" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
              Dashboard
            </a>
          </li>
          <li>
            <a routerLink="/website/pages" routerLinkActive="active">
              Pages
            </a>
          </li>
          <li>
            <a routerLink="/website/news" routerLinkActive="active">
              News
            </a>
          </li>
          <li>
            <a routerLink="/website/events" routerLinkActive="active">
              Events
            </a>
          </li>
          <li>
            <a routerLink="/website/albums" routerLinkActive="active">
              Albums
            </a>
          </li>
          <li>
            <a routerLink="/website/contacts" routerLinkActive="active">
              Contacts
            </a>
          </li>
          <li>
            <a routerLink="/website/subscribers" routerLinkActive="active">
              Subscribers
            </a>
          </li>
          <li>
            <a routerLink="/website/widgets" routerLinkActive="active">
              Widgets
            </a>
          </li>
          <li>
            <a routerLink="/website/notifications" routerLinkActive="active">
              Notifications
              <span *ngIf="unreadCount > 0" class="badge">{{ unreadCount }}</span>
            </a>
          </li>
        </ul>
        <div class="sidebar-footer">
          <div class="user-info">
            <p>{{ currentUser?.name }}</p>
            <p class="user-email">{{ currentUser?.email }}</p>
          </div>
          <div class="notifications">
            <a routerLink="/website/notifications" class="notification-bell">
              🔔
              <span *ngIf="unreadCount > 0" class="badge">{{ unreadCount }}</span>
            </a>
          </div>
          <button class="btn btn-secondary" (click)="logout()">Logout</button>
        </div>
      </nav>
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .layout-container {
      display: flex;
      min-height: 100vh;
    }

    .sidebar {
      width: 250px;
      background-color: #2c3e50;
      color: white;
      display: flex;
      flex-direction: column;
    }

    .sidebar-header {
      padding: 20px;
      border-bottom: 1px solid #34495e;
    }

    .sidebar-header h3 {
      margin: 0;
    }

    .nav-menu {
      list-style: none;
      padding: 0;
      margin: 0;
      flex: 1;
    }

    .nav-menu li {
      border-bottom: 1px solid #34495e;
    }

    .nav-menu a {
      display: block;
      padding: 15px 20px;
      color: white;
      text-decoration: none;
      transition: background-color 0.3s;
    }

    .nav-menu a:hover,
    .nav-menu a.active {
      background-color: #34495e;
    }

    .sidebar-footer {
      padding: 20px;
      border-top: 1px solid #34495e;
    }

    .user-info {
      margin-bottom: 15px;
    }

    .user-info p {
      margin: 5px 0;
    }

    .user-email {
      font-size: 12px;
      color: #bdc3c7;
    }

    .notifications {
      margin-bottom: 15px;
    }

    .notification-bell {
      position: relative;
      display: inline-block;
      font-size: 20px;
      text-decoration: none;
      color: white;
    }

    .notification-bell .badge {
      position: absolute;
      top: -8px;
      right: -8px;
      background-color: #dc3545;
      color: white;
      border-radius: 50%;
      width: 20px;
      height: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
    }

    .main-content {
      flex: 1;
      padding: 30px;
      background-color: #f5f5f5;
    }

    .btn {
      width: 100%;
      padding: 10px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
    }

    .btn-secondary {
      background-color: #6c757d;
      color: white;
    }
  `]
})
export class WebsiteLayoutComponent {
  currentUser: any;
  unreadCount = 0;

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService
  ) {
    this.notificationService.unreadCount$.subscribe(count => {
      this.unreadCount = count;
    });
  }

  logout(): void {
    this.authService.logout();
    window.location.href = '/login';
  }
}

