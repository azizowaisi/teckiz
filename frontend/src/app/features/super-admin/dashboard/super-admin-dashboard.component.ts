import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-super-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="dashboard-container">
      <nav class="sidebar">
        <div class="sidebar-header">
          <h3>Teckiz</h3>
        </div>
        <ul class="nav-menu">
          <li>
            <a routerLink="/superadmin/index" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
              Overview
            </a>
          </li>
          <li>
            <a routerLink="/superadmin/companies" routerLinkActive="active">
              Companies
            </a>
          </li>
          <li>
            <a routerLink="/superadmin/users" routerLinkActive="active">
              Users
            </a>
          </li>
          <li>
            <a routerLink="/superadmin/modules" routerLinkActive="active">
              Modules
            </a>
          </li>
          <li>
            <a routerLink="/superadmin/email-templates" routerLinkActive="active">
              Email Templates
            </a>
          </li>
          <li>
            <a routerLink="/superadmin/invoices" routerLinkActive="active">
              Invoices
            </a>
          </li>
          <li>
            <a routerLink="/superadmin/notification-requests" routerLinkActive="active">
              Notification Requests
            </a>
          </li>
        </ul>
        <div class="sidebar-footer">
          <div class="user-info">
            <p>{{ currentUser?.name }}</p>
            <p class="user-email">{{ currentUser?.email }}</p>
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
    .dashboard-container {
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

    .main-content {
      flex: 1;
      padding: 30px;
      background-color: #f5f5f5;
    }
  `]
})
export class SuperAdminDashboardComponent {
  currentUser = this.authService.getCurrentUser();

  constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
    window.location.href = '/login';
  }
}

