import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

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
  imports: [CommonModule],
  template: `
    <div class="users-container">
      <h1>Users Management</h1>
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
  users: User[] = [];
  loading = false;
  error = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.http.get<{ users: User[] }>(`${environment.apiUrl}/superadmin/users`).subscribe({
      next: (response) => {
        this.users = response.users || [];
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load users';
        this.loading = false;
      }
    });
  }
}

