import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ResearchArticleService } from '../../../core/services/researcharticle.service';
import { ResearchJournalService } from '../../../core/services/researchjournal.service';
import { ResearchArticleResponse, ResearchArticleRequest } from '../../../core/models/researcharticle.model';
import { ResearchJournalResponse } from '../../../core/models/researchjournal.model';

@Component({
  selector: 'app-research-articles',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="articles-container">
      <div class="header">
        <h1>Research Articles Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Article</button>
      </div>

      <div class="filters">
        <label>Filter by Journal:</label>
        <select [(ngModel)]="selectedJournalId" (change)="loadArticles()">
          <option [value]="null">All Journals</option>
          <option *ngFor="let journal of journals" [value]="journal.id">
            {{ journal.title }}
          </option>
        </select>
      </div>

      <div *ngIf="showCreateForm || editingArticle" class="form-container">
        <h2>{{ editingArticle ? 'Edit Article' : 'Create New Article' }}</h2>
        <form [formGroup]="articleForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Research Journal *</label>
            <select formControlName="researchJournalId">
              <option value="">Select a journal</option>
              <option *ngFor="let journal of journals" [value]="journal.id">
                {{ journal.title }}
              </option>
            </select>
            <div *ngIf="articleForm.get('researchJournalId')?.invalid && articleForm.get('researchJournalId')?.touched" class="error">
              Research journal is required
            </div>
          </div>

          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="articleForm.get('title')?.invalid && articleForm.get('title')?.touched" class="error">
              Title is required
            </div>
          </div>

          <div class="form-group">
            <label>Slug</label>
            <input type="text" formControlName="slug" />
          </div>

          <div class="form-group">
            <label>Abstract</label>
            <textarea formControlName="abstract" rows="4"></textarea>
          </div>

          <div class="form-group">
            <label>Keywords</label>
            <input type="text" formControlName="keywords" placeholder="Comma-separated keywords" />
          </div>

          <div class="form-group">
            <label>DOI</label>
            <input type="text" formControlName="doi" />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Page Start</label>
              <input type="number" formControlName="pageStart" />
            </div>
            <div class="form-group">
              <label>Page End</label>
              <input type="number" formControlName="pageEnd" />
            </div>
          </div>

          <div class="form-group">
            <label>
              <input type="checkbox" formControlName="published" />
              Published
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="articleForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingArticle ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading articles...</div>

      <div class="articles-list" *ngIf="articles.length > 0">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Journal</th>
              <th>DOI</th>
              <th>Status</th>
              <th>Published</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let article of articles">
              <td>{{ article.title }}</td>
              <td>{{ getJournalName(article.researchJournalId) }}</td>
              <td>{{ article.doi || '-' }}</td>
              <td>
                <span [class]="article.published ? 'badge published' : 'badge draft'">
                  {{ article.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>{{ article.publishedAt | date:'short' }}</td>
              <td>
                <a [routerLink]="['/journal/research-articles', article.articleKey, 'authors']" class="btn btn-sm btn-info">Authors</a>
                <button class="btn btn-sm btn-primary" (click)="editArticle(article)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteArticle(article)">Delete</button>
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
    .articles-container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .filters {
      margin-bottom: 20px;
      padding: 15px;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .filters label {
      margin-right: 10px;
      font-weight: 500;
    }

    .filters select {
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .form-container {
      background: white;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 20px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 15px;
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
    .form-group textarea,
    .form-group select {
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

    .articles-list {
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
export class ResearchArticlesComponent implements OnInit {
  articles: ResearchArticleResponse[] = [];
  journals: ResearchJournalResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingArticle: ResearchArticleResponse | null = null;
  articleForm: FormGroup;
  currentPage = 0;
  totalPages = 0;
  selectedJournalId: number | null = null;

  constructor(
    private researchArticleService: ResearchArticleService,
    private researchJournalService: ResearchJournalService,
    private fb: FormBuilder
  ) {
    this.articleForm = this.fb.group({
      researchJournalId: ['', Validators.required],
      title: ['', Validators.required],
      slug: [''],
      abstract: [''],
      keywords: [''],
      doi: [''],
      pageStart: [null],
      pageEnd: [null],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadJournals();
    this.loadArticles();
  }

  loadJournals(): void {
    this.researchJournalService.listJournals(0, 100).subscribe({
      next: (response) => {
        this.journals = response.journals || [];
      }
    });
  }

  loadArticles(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.researchArticleService.listArticles(page, 20, this.selectedJournalId || undefined).subscribe({
      next: (response) => {
        this.articles = response.articles || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load articles';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadArticles(page);
  }

  getJournalName(journalId: number): string {
    const journal = this.journals.find(j => j.id === journalId);
    return journal ? journal.title : 'Unknown';
  }

  onSubmit(): void {
    if (this.articleForm.valid) {
      this.loading = true;
      const request: ResearchArticleRequest = this.articleForm.value;

      if (this.editingArticle) {
        this.researchArticleService.updateArticle(this.editingArticle.articleKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadArticles(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update article';
            this.loading = false;
          }
        });
      } else {
        this.researchArticleService.createArticle(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadArticles();
          },
          error: () => {
            this.error = 'Failed to create article';
            this.loading = false;
          }
        });
      }
    }
  }

  editArticle(article: ResearchArticleResponse): void {
    this.editingArticle = article;
    this.articleForm.patchValue({
      researchJournalId: article.researchJournalId,
      title: article.title,
      slug: article.slug,
      abstract: article.abstract || '',
      keywords: article.keywords || '',
      doi: article.doi || '',
      pageStart: article.pageStart || null,
      pageEnd: article.pageEnd || null,
      published: article.published
    });
    this.showCreateForm = true;
  }

  deleteArticle(article: ResearchArticleResponse): void {
    if (confirm(`Delete "${article.title}"?`)) {
      this.loading = true;
      this.researchArticleService.deleteArticle(article.articleKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadArticles(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete article';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingArticle = null;
    this.articleForm.reset({ published: false });
  }
}

