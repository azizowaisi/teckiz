import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ResearchArticleAuthorService } from '../../../core/services/researcharticleauthor.service';
import { ResearchArticleAuthorResponse, ResearchArticleAuthorRequest } from '../../../core/models/researcharticleauthor.model';

@Component({
  selector: 'app-article-authors',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="authors-container">
      <div class="header">
        <h1>Article Authors: {{ articleTitle }}</h1>
        <button class="btn btn-secondary" (click)="goBack()">Back to Articles</button>
        <button class="btn btn-primary" (click)="showCreateForm = true">Add Author</button>
      </div>

      <div *ngIf="showCreateForm || editingAuthor" class="form-container">
        <h2>{{ editingAuthor ? 'Edit Author' : 'Add New Author' }}</h2>
        <form [formGroup]="authorForm" (ngSubmit)="onSubmit()">
          <div class="form-row">
            <div class="form-group">
              <label>First Name *</label>
              <input type="text" formControlName="firstName" />
              <div *ngIf="authorForm.get('firstName')?.invalid && authorForm.get('firstName')?.touched" class="error">
                First name is required
              </div>
            </div>
            <div class="form-group">
              <label>Last Name *</label>
              <input type="text" formControlName="lastName" />
              <div *ngIf="authorForm.get('lastName')?.invalid && authorForm.get('lastName')?.touched" class="error">
                Last name is required
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>Email</label>
            <input type="email" formControlName="email" />
          </div>

          <div class="form-group">
            <label>Affiliation</label>
            <input type="text" formControlName="affiliation" />
          </div>

          <div class="form-group">
            <label>ORCID</label>
            <input type="text" formControlName="orcid" />
          </div>

          <div class="form-group">
            <label>Order *</label>
            <input type="number" formControlName="order" min="1" />
            <div *ngIf="authorForm.get('order')?.invalid && authorForm.get('order')?.touched" class="error">
              Order is required
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="authorForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingAuthor ? 'Update' : 'Add') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading authors...</div>

      <div class="authors-list" *ngIf="authors.length > 0">
        <table>
          <thead>
            <tr>
              <th>Order</th>
              <th>Name</th>
              <th>Email</th>
              <th>Affiliation</th>
              <th>ORCID</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let author of authors">
              <td>{{ author.order }}</td>
              <td>{{ author.firstName }} {{ author.lastName }}</td>
              <td>{{ author.email || '-' }}</td>
              <td>{{ author.affiliation || '-' }}</td>
              <td>{{ author.orcid || '-' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editAuthor(author)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteAuthor(author)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .authors-container {
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

    .form-group input {
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

    .authors-list {
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
  `]
})
export class ArticleAuthorsComponent implements OnInit {
  articleKey: string = '';
  articleTitle: string = '';
  authors: ResearchArticleAuthorResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingAuthor: ResearchArticleAuthorResponse | null = null;
  authorForm: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private researchArticleAuthorService: ResearchArticleAuthorService,
    private fb: FormBuilder
  ) {
    this.authorForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: [''],
      affiliation: [''],
      orcid: [''],
      order: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.articleKey = this.route.snapshot.paramMap.get('articleKey') || '';
    if (this.articleKey) {
      this.loadAuthors();
    }
  }

  loadAuthors(): void {
    this.loading = true;
    this.error = '';
    this.researchArticleAuthorService.listAuthors(this.articleKey).subscribe({
      next: (response) => {
        this.authors = (response.authors || []).sort((a, b) => a.order - b.order);
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load authors';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.authorForm.valid) {
      this.loading = true;
      const request: ResearchArticleAuthorRequest = {
        ...this.authorForm.value,
        researchArticleId: 0 // Will be set by backend
      };

      if (this.editingAuthor && this.editingAuthor.authorKey) {
        this.researchArticleAuthorService.updateAuthor(this.editingAuthor.authorKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadAuthors();
          },
          error: () => {
            this.error = 'Failed to update author';
            this.loading = false;
          }
        });
      } else {
        this.researchArticleAuthorService.createAuthor(this.articleKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadAuthors();
          },
          error: () => {
            this.error = 'Failed to add author';
            this.loading = false;
          }
        });
      }
    }
  }

  editAuthor(author: ResearchArticleAuthorResponse): void {
    this.editingAuthor = author;
    this.authorForm.patchValue({
      firstName: author.firstName,
      lastName: author.lastName,
      email: author.email || '',
      affiliation: author.affiliation || '',
      orcid: author.orcid || '',
      order: author.order
    });
    this.showCreateForm = true;
  }

  deleteAuthor(author: ResearchArticleAuthorResponse): void {
    if (confirm(`Delete author ${author.firstName} ${author.lastName}?`) && author.authorKey) {
      this.loading = true;
      this.researchArticleAuthorService.deleteAuthor(author.authorKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadAuthors();
        },
        error: () => {
          this.error = 'Failed to delete author';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingAuthor = null;
    this.authorForm.reset({ order: 1 });
  }

  goBack(): void {
    window.history.back();
  }
}

