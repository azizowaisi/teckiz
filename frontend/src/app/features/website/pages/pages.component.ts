import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { WebPageService } from '../../../core/services/webpage.service';
import { WebPageResponse, WebPageRequest } from '../../../core/models/webpage.model';

@Component({
  selector: 'app-pages',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="pages-container">
      <div class="header">
        <h1>Web Pages Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Page</button>
      </div>

      <div *ngIf="showCreateForm || editingPage" class="form-container">
        <h2>{{ editingPage ? 'Edit Page' : 'Create New Page' }}</h2>
        <form [formGroup]="pageForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="pageForm.get('title')?.invalid && pageForm.get('title')?.touched" class="error">
              Title is required
            </div>
          </div>

          <div class="form-group">
            <label>Slug</label>
            <input type="text" formControlName="slug" />
            <small>Leave empty to auto-generate from title</small>
          </div>

          <div class="form-group">
            <label>Short Description</label>
            <textarea formControlName="shortDescription" rows="2"></textarea>
          </div>

          <div class="form-group">
            <label>Description</label>
            <textarea formControlName="description" rows="6"></textarea>
          </div>

          <div class="form-group">
            <label>Thumbnail URL</label>
            <input type="text" formControlName="thumbnail" />
          </div>

          <div class="form-group">
            <label>
              <input type="checkbox" formControlName="published" />
              Published
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="pageForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingPage ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading pages...</div>

      <div class="pages-list" *ngIf="pages.length > 0">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Slug</th>
              <th>Status</th>
              <th>Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let page of pages">
              <td>{{ page.title }}</td>
              <td>{{ page.slug }}</td>
              <td>
                <span [class]="page.published ? 'badge published' : 'badge draft'">
                  {{ page.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>{{ page.updatedAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editPage(page)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deletePage(page)">Delete</button>
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
    .pages-container {
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
      box-sizing: border-box;
    }

    .form-actions {
      display: flex;
      gap: 10px;
      margin-top: 20px;
    }

    .pages-list {
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

    .badge.published {
      background-color: #28a745;
      color: white;
    }

    .badge.draft {
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
export class PagesComponent implements OnInit {
  pages: WebPageResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingPage: WebPageResponse | null = null;
  pageForm: FormGroup;
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;

  constructor(
    private webpageService: WebPageService,
    private fb: FormBuilder
  ) {
    this.pageForm = this.fb.group({
      title: ['', Validators.required],
      slug: [''],
      shortDescription: [''],
      description: [''],
      thumbnail: [''],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadPages();
  }

  loadPages(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.webpageService.listPages(page, 20).subscribe({
      next: (response) => {
        this.pages = response.pages || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.totalElements = response.totalElements || 0;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load pages';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadPages(page);
  }

  onSubmit(): void {
    if (this.pageForm.valid) {
      this.loading = true;
      const request: WebPageRequest = this.pageForm.value;

      if (this.editingPage) {
        this.webpageService.updatePage(this.editingPage.pageKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadPages(this.currentPage);
          },
          error: (error) => {
            this.error = 'Failed to update page';
            this.loading = false;
          }
        });
      } else {
        this.webpageService.createPage(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadPages();
          },
          error: (error) => {
            this.error = 'Failed to create page';
            this.loading = false;
          }
        });
      }
    }
  }

  editPage(page: WebPageResponse): void {
    this.editingPage = page;
    this.pageForm.patchValue({
      title: page.title,
      slug: page.slug,
      shortDescription: page.shortDescription || '',
      description: page.description || '',
      thumbnail: page.thumbnail || '',
      published: page.published
    });
    this.showCreateForm = true;
  }

  deletePage(page: WebPageResponse): void {
    if (confirm(`Are you sure you want to delete "${page.title}"?`)) {
      this.loading = true;
      this.webpageService.deletePage(page.pageKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadPages(this.currentPage);
        },
        error: (error) => {
          this.error = 'Failed to delete page';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingPage = null;
    this.pageForm.reset({ published: false });
  }
}

