import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface EmailTemplate {
  id: number;
  templateKey: string;
  name: string;
  subject: string;
  body: string;
  templateType?: string;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

@Component({
  selector: 'app-email-templates',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="templates-container">
      <div class="header">
        <h1>Email Templates Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Template</button>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading" class="loading">Loading templates...</div>

      <div class="templates-list" *ngIf="templates.length > 0">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Subject</th>
              <th>Type</th>
              <th>Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let template of templates">
              <td>{{ template.name }}</td>
              <td>{{ template.subject }}</td>
              <td>{{ template.templateType || '-' }}</td>
              <td>{{ template.updatedAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="viewTemplate(template)">View</button>
                <button class="btn btn-sm btn-danger" (click)="deleteTemplate(template)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div *ngIf="selectedTemplate" class="modal-overlay" (click)="closeModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>{{ selectedTemplate.name }}</h2>
            <button class="btn-close" (click)="closeModal()">×</button>
          </div>
          <div class="modal-body">
            <p><strong>Subject:</strong> {{ selectedTemplate.subject }}</p>
            <p><strong>Type:</strong> {{ selectedTemplate.templateType || '-' }}</p>
            <p><strong>Body:</strong></p>
            <div class="template-body">{{ selectedTemplate.body }}</div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .templates-container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .templates-list {
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
      max-width: 700px;
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

    .template-body {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 4px;
      margin: 10px 0;
      white-space: pre-wrap;
      max-height: 400px;
      overflow-y: auto;
    }
  `]
})
export class EmailTemplatesComponent implements OnInit {
  templates: EmailTemplate[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  selectedTemplate: EmailTemplate | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadTemplates();
  }

  loadTemplates(): void {
    this.loading = true;
    this.http.get<{ emailTemplates: EmailTemplate[] }>(`${environment.apiUrl}/superadmin/email-templates`).subscribe({
      next: (response) => {
        this.templates = response.emailTemplates || [];
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load email templates';
        this.loading = false;
      }
    });
  }

  viewTemplate(template: EmailTemplate): void {
    this.selectedTemplate = template;
  }

  closeModal(): void {
    this.selectedTemplate = null;
  }

  deleteTemplate(template: EmailTemplate): void {
    if (confirm(`Delete template "${template.name}"?`)) {
      this.loading = true;
      this.http.delete<{ message: string }>(`${environment.apiUrl}/superadmin/email-templates/${template.templateKey}`).subscribe({
        next: () => {
          this.loading = false;
          this.loadTemplates();
        },
        error: () => {
          this.error = 'Failed to delete template';
          this.loading = false;
        }
      });
    }
  }
}

