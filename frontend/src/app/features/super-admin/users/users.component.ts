import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { User as UserModel } from '../../../core/models/user.model';

interface User {
  id: number;
  email: string;
  name: string;
  isEnabled: boolean;
  isSuperAdmin: boolean;
}

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="users-container">
      <div class="header">
        <h1>Users Management</h1>
        <div class="search-box">
          <input type="text" [(ngModel)]="searchTerm" placeholder="Search users..." (keyup.enter)="onSearch()" />
          <button class="btn btn-primary" (click)="onSearch()">Search</button>
        </div>
      </div>
      <div *ngIf="loading" class="loading">Loading users...</div>
      <div *ngIf="error" class="error-message">{{ error }}</div>
      <table *ngIf="users.length > 0" class="users-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Status</th>
            <th>Super Admin</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let user of users">
            <td>{{ user.id }}</td>
            <td>{{ user.name }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.isEnabled ? 'Enabled' : 'Disabled' }}</td>
            <td>{{ user.isSuperAdmin ? 'Yes' : 'No' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .users-container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .search-box {
      display: flex;
      gap: 10px;
    }

    .search-box input {
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .btn {
      padding: 8px 16px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }

    .btn-primary {
      background-color: #007bff;
      color: white;
    }

    .users-table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 20px;
      background: white;
      border-radius: 4px;
      overflow: hidden;
    }

    .users-table th,
    .users-table td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }

    .users-table th {
      background-color: #f8f9fa;
      font-weight: 600;
    }

    .users-table tr:hover {
      background-color: #f5f5f5;
    }
  `]
})
export class UsersComponent implements OnInit {
  users: UserInterface[] = [];
  loading = false;
  error = '';
  searchTerm = '';

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.userService.getAllUsers(this.searchTerm).subscribe({
      next: (response) => {
        this.users = response.users || [];
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load users';
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.loadUsers();
  }

