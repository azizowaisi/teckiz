import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CompanyService } from '../../../core/services/company.service';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-company-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="company-users-container">
      <div class="header">
        <h1>Company Users: {{ companyName }}</h1>
        <button class="btn btn-secondary" (click)="goBack()">Back to Companies</button>
        <button class="btn btn-primary" (click)="showAddForm = true">Add User</button>
      </div>

      <div *ngIf="showAddForm" class="form-container">
        <h2>Add User to Company</h2>
        <form [formGroup]="addUserForm" (ngSubmit)="onAddUser()">
          <div class="form-group">
            <label>User *</label>
            <select formControlName="userId" (change)="onUserSelect()">
              <option value="">Select a user</option>
              <option *ngFor="let user of availableUsers" [value]="user.id">
                {{ user.name }} ({{ user.email }})
              </option>
            </select>
            <div *ngIf="addUserForm.get('userId')?.invalid && addUserForm.get('userId')?.touched" class="error">
              User is required
            </div>
          </div>

          <div class="form-group">
            <label>Role *</label>
            <select formControlName="roleId">
              <option value="">Select a role</option>
              <option *ngFor="let role of roles" [value]="role.id">
                {{ role.name }}
              </option>
            </select>
            <div *ngIf="addUserForm.get('roleId')?.invalid && addUserForm.get('roleId')?.touched" class="error">
              Role is required
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="addUserForm.invalid || loading">
              {{ loading ? 'Adding...' : 'Add User' }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="showAddForm = false">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showAddForm" class="loading">Loading users...</div>

      <div class="users-table" *ngIf="users.length > 0">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let user of users">
              <td>{{ user.name }}</td>
              <td>{{ user.email }}</td>
              <td>
                <span [class]="user.isEnabled ? 'badge active' : 'badge inactive'">
                  {{ user.isEnabled ? 'Enabled' : 'Disabled' }}
                </span>
              </td>
              <td>
                <button class="btn btn-sm btn-danger" (click)="removeUser(user)">Remove</button>
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
    .company-users-container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      gap: 10px;
    }

    .form-container {
      background: white;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 20px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .form-group {
      margin-bottom: 15px;
    }

    .form-group label {
      display: block;
      margin-bottom: 5px;
      font-weight: 500;
    }

    .form-group select {
      width: 100%;
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .form-actions {
      display: flex;
      gap: 10px;
      margin-top: 20px;
    }

    .users-table {
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

    .badge.active {
      background-color: #28a745;
      color: white;
    }

    .badge.inactive {
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

    .btn-secondary {
      background-color: #6c757d;
      color: white;
    }

    .btn-danger {
      background-color: #dc3545;
      color: white;
    }

    .btn-sm {
      padding: 4px 8px;
      font-size: 12px;
    }

    .error {
      color: #dc3545;
      font-size: 12px;
      margin-top: 4px;
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
export class CompanyUsersComponent implements OnInit {
  companyKey: string = '';
  companyName: string = '';
  users: any[] = [];
  availableUsers: User[] = [];
  roles: any[] = [];
  loading = false;
  error = '';
  showAddForm = false;
  addUserForm: FormGroup;
  currentPage = 0;
  totalPages = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private companyService: CompanyService,
    private userService: UserService,
    private fb: FormBuilder
  ) {
    this.addUserForm = this.fb.group({
      userId: ['', Validators.required],
      roleId: ['', Validators.required],
      moduleIds: [[]]
    });
  }

  ngOnInit(): void {
    this.companyKey = this.route.snapshot.paramMap.get('companyKey') || '';
    if (this.companyKey) {
      this.loadCompanyUsers();
      this.loadAvailableUsers();
      this.loadRoles();
      this.loadCompany();
    }
  }

  loadCompany(): void {
    this.companyService.getCompany(this.companyKey).subscribe({
      next: (company) => {
        this.companyName = company.name;
      }
    });
  }

  loadCompanyUsers(): void {
    this.loading = true;
    this.companyService.getCompanyUsers(this.companyKey, this.currentPage, 20).subscribe({
      next: (response) => {
        this.users = response.users || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load company users';
        this.loading = false;
      }
    });
  }

  loadAvailableUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (response) => {
        this.availableUsers = response.users || [];
      }
    });
  }

  loadRoles(): void {
    this.companyService.getCompanyRoles(this.companyKey).subscribe({
      next: (response) => {
        this.roles = response.roles || [];
      }
    });
  }

  loadPage(page: number): void {
    this.currentPage = page;
    this.loadCompanyUsers();
  }

  onUserSelect(): void {
    // Could load user's current roles/modules here
  }

  onAddUser(): void {
    if (this.addUserForm.valid) {
      this.loading = true;
      const request = this.addUserForm.value;
      this.companyService.addUserToCompany(this.companyKey, request).subscribe({
        next: () => {
          this.loading = false;
          this.showAddForm = false;
          this.addUserForm.reset();
          this.loadCompanyUsers();
        },
        error: () => {
          this.error = 'Failed to add user to company';
          this.loading = false;
        }
      });
    }
  }

  removeUser(user: any): void {
    if (confirm(`Remove ${user.name} from this company?`)) {
      this.loading = true;
      this.companyService.removeUserFromCompany(this.companyKey, user.id).subscribe({
        next: () => {
          this.loading = false;
          this.loadCompanyUsers();
        },
        error: () => {
          this.error = 'Failed to remove user';
          this.loading = false;
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/superadmin/companies']);
  }
}

