import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { WebNewsService } from '../../../core/services/webnews.service';
import { WebNewsResponse, WebNewsRequest } from '../../../core/models/webnews.model';

@Component({
  selector: 'app-news',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="news-container">
      <div class="header">
        <h1>News Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Article</button>
      </div>

      <div *ngIf="showCreateForm || editingNews" class="form-container">
        <h2>{{ editingNews ? 'Edit News Article' : 'Create New News Article' }}</h2>
        <form [formGroup]="newsForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="newsForm.get('title')?.invalid && newsForm.get('title')?.touched" class="error">
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
            <label>
              <input type="checkbox" formControlName="published" />
              Published
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="newsForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingNews ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading news...</div>

      <div class="news-list" *ngIf="news.length > 0">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Slug</th>
              <th>Status</th>
              <th>Published</th>
              <th>Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of news">
              <td>{{ item.title }}</td>
              <td>{{ item.slug }}</td>
              <td>
                <span [class]="item.published ? 'badge published' : 'badge draft'">
                  {{ item.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>{{ item.publishedAt | date:'short' }}</td>
              <td>{{ item.updatedAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editNews(item)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteNews(item)">Delete</button>
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
    .news-container {
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

    .news-list {
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
export class NewsComponent implements OnInit {
  news: WebNewsResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingNews: WebNewsResponse | null = null;
  newsForm: FormGroup;
  currentPage = 0;
  totalPages = 0;

  constructor(
    private webNewsService: WebNewsService,
    private fb: FormBuilder
  ) {
    this.newsForm = this.fb.group({
      title: ['', Validators.required],
      slug: [''],
      shortDescription: [''],
      description: [''],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadNews();
  }

  loadNews(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.webNewsService.listNews(page, 20).subscribe({
      next: (response) => {
        this.news = response.news || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load news';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadNews(page);
  }

  onSubmit(): void {
    if (this.newsForm.valid) {
      this.loading = true;
      const request: WebNewsRequest = this.newsForm.value;

      if (this.editingNews) {
        this.webNewsService.updateNews(this.editingNews.newsKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadNews(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update news';
            this.loading = false;
          }
        });
      } else {
        this.webNewsService.createNews(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadNews();
          },
          error: () => {
            this.error = 'Failed to create news';
            this.loading = false;
          }
        });
      }
    }
  }

  editNews(news: WebNewsResponse): void {
    this.editingNews = news;
    this.newsForm.patchValue({
      title: news.title,
      slug: news.slug,
      shortDescription: news.shortDescription || '',
      description: news.description || '',
      published: news.published
    });
    this.showCreateForm = true;
  }

  deleteNews(news: WebNewsResponse): void {
    if (confirm(`Delete "${news.title}"?`)) {
      this.loading = true;
      this.webNewsService.deleteNews(news.newsKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadNews(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete news';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingNews = null;
    this.newsForm.reset({ published: false });
  }
}

