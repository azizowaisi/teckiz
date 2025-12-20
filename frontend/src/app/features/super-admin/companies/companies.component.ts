import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CompanyService } from '../../../core/services/company.service';
import { CompanyResponse, CompanyRequest } from '../../../core/models/company.model';

@Component({
  selector: 'app-companies',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="companies-container">
      <div class="header">
        <h1>Companies Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Add New Company</button>
      </div>

      <div *ngIf="showCreateForm || editingCompany" class="form-container">
        <h2>{{ editingCompany ? 'Edit Company' : 'Create New Company' }}</h2>
        <form [formGroup]="companyForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Name *</label>
            <input type="text" formControlName="name" />
            <div *ngIf="companyForm.get('name')?.invalid && companyForm.get('name')?.touched" class="error">
              Name is required
            </div>
          </div>

          <div class="form-group">
            <label>Slug</label>
            <input type="text" formControlName="slug" />
            <small>Leave empty to auto-generate from name</small>
          </div>

          <div class="form-group">
            <label>Email</label>
            <input type="email" formControlName="email" />
          </div>

          <div class="form-group">
            <label>Phone</label>
            <input type="text" formControlName="phone" />
          </div>

          <div class="form-group">
            <label>Address</label>
            <input type="text" formControlName="address" />
          </div>

          <div class="form-group">
            <label>City</label>
            <input type="text" formControlName="city" />
          </div>

          <div class="form-group">
            <label>Country</label>
            <input type="text" formControlName="country" maxlength="2" />
          </div>

          <div class="form-group">
            <label>Description</label>
            <textarea formControlName="description" rows="4"></textarea>
          </div>

          <div class="form-group">
            <label>
              <input type="checkbox" formControlName="active" />
              Active
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="companyForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingCompany ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading companies...</div>

      <div *ngIf="companies.length > 0" class="companies-table">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Slug</th>
              <th>Email</th>
              <th>Status</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let company of companies">
              <td>{{ company.name }}</td>
              <td>{{ company.slug }}</td>
              <td>{{ company.email || '-' }}</td>
              <td>
                <span [class]="company.active ? 'badge active' : 'badge inactive'">
                  {{ company.active ? 'Active' : 'Inactive' }}
                </span>
              </td>
              <td>{{ company.createdAt | date:'short' }}</td>
              <td>
                <a [routerLink]="['/superadmin/companies', company.companyKey, 'users']" class="btn btn-sm btn-info">Users</a>
                <button class="btn btn-sm btn-primary" (click)="editCompany(company)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteCompany(company)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .companies-container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
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

    .form-group input,
    .form-group textarea {
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

    .companies-table {
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

    .btn-info {
      background-color: #17a2b8;
      color: white;
      text-decoration: none;
      display: inline-block;
    }

    .btn-sm {
      padding: 4px 8px;
      font-size: 12px;
      margin-right: 5px;
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
  `]
})
export class CompaniesComponent implements OnInit {
  companies: CompanyResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingCompany: CompanyResponse | null = null;
  companyForm: FormGroup;

  constructor(
    private companyService: CompanyService,
    private fb: FormBuilder
  ) {
    this.companyForm = this.fb.group({
      name: ['', Validators.required],
      slug: [''],
      email: [''],
      phone: [''],
      address: [''],
      city: [''],
      country: [''],
      description: [''],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.loadCompanies();
  }

  loadCompanies(): void {
    this.loading = true;
    this.error = '';
    this.companyService.getAllCompanies().subscribe({
      next: (response) => {
        this.companies = response.companies || [];
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load companies';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.companyForm.valid) {
      this.loading = true;
      const request: CompanyRequest = this.companyForm.value;

      if (this.editingCompany) {
        this.companyService.updateCompany(this.editingCompany.companyKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadCompanies();
          },
          error: (error) => {
            this.error = 'Failed to update company';
            this.loading = false;
          }
        });
      } else {
        this.companyService.createCompany(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadCompanies();
          },
          error: (error) => {
            this.error = 'Failed to create company';
            this.loading = false;
          }
        });
      }
    }
  }

  editCompany(company: CompanyResponse): void {
    this.editingCompany = company;
    this.companyForm.patchValue({
      name: company.name,
      slug: company.slug,
      email: company.email || '',
      phone: company.phone || '',
      address: company.address || '',
      city: company.city || '',
      country: company.country || '',
      description: company.description || '',
      active: company.active
    });
    this.showCreateForm = true;
  }

  deleteCompany(company: CompanyResponse): void {
    if (confirm(`Are you sure you want to delete ${company.name}?`)) {
      this.loading = true;
      this.companyService.deleteCompany(company.companyKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadCompanies();
        },
        error: (error) => {
          this.error = 'Failed to delete company';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingCompany = null;
    this.companyForm.reset({ active: true });
  }
}

