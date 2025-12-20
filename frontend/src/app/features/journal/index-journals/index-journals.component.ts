import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { IndexJournalService } from '../../../core/services/indexjournal.service';
import { IndexJournalResponse, IndexJournalRequest } from '../../../core/models/indexjournal.model';

@Component({
  selector: 'app-index-journals',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="journals-container">
      <div class="header">
        <h1>Index Journals Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Journal</button>
      </div>

      <div *ngIf="showCreateForm || editingJournal" class="form-container">
        <h2>{{ editingJournal ? 'Edit Index Journal' : 'Create New Index Journal' }}</h2>
        <form [formGroup]="journalForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="journalForm.get('title')?.invalid && journalForm.get('title')?.touched" class="error">
              Title is required
            </div>
          </div>

          <div class="form-group">
            <label>Slug</label>
            <input type="text" formControlName="slug" />
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
            <button type="submit" class="btn btn-primary" [disabled]="journalForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingJournal ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading journals...</div>

      <div class="journals-list" *ngIf="journals.length > 0">
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
            <tr *ngFor="let journal of journals">
              <td>{{ journal.title }}</td>
              <td>{{ journal.slug }}</td>
              <td>
                <span [class]="journal.published ? 'badge published' : 'badge draft'">
                  {{ journal.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>{{ journal.updatedAt | date:'short' }}</td>
              <td>
                <a [routerLink]="['/journal/index-journals', journal.journalKey, 'volumes']" class="btn btn-sm btn-info">Volumes</a>
                <button class="btn btn-sm btn-primary" (click)="editJournal(journal)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteJournal(journal)">Delete</button>
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
    .journals-container {
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

    .journals-list {
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
export class IndexJournalsComponent implements OnInit {
  journals: IndexJournalResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingJournal: IndexJournalResponse | null = null;
  journalForm: FormGroup;
  currentPage = 0;
  totalPages = 0;

  constructor(
    private indexJournalService: IndexJournalService,
    private fb: FormBuilder
  ) {
    this.journalForm = this.fb.group({
      title: ['', Validators.required],
      slug: [''],
      shortDescription: [''],
      description: [''],
      thumbnail: [''],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadJournals();
  }

  loadJournals(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.indexJournalService.listJournals(page, 20).subscribe({
      next: (response) => {
        this.journals = response.indexJournals || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load journals';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadJournals(page);
  }

  onSubmit(): void {
    if (this.journalForm.valid) {
      this.loading = true;
      const request: IndexJournalRequest = this.journalForm.value;

      if (this.editingJournal) {
        this.indexJournalService.updateJournal(this.editingJournal.journalKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadJournals(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update journal';
            this.loading = false;
          }
        });
      } else {
        this.indexJournalService.createJournal(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadJournals();
          },
          error: () => {
            this.error = 'Failed to create journal';
            this.loading = false;
          }
        });
      }
    }
  }

  editJournal(journal: IndexJournalResponse): void {
    this.editingJournal = journal;
    this.journalForm.patchValue({
      title: journal.title,
      slug: journal.slug,
      shortDescription: journal.shortDescription || '',
      description: journal.description || '',
      thumbnail: journal.thumbnail || '',
      published: journal.published
    });
    this.showCreateForm = true;
  }

  deleteJournal(journal: IndexJournalResponse): void {
    if (confirm(`Delete "${journal.title}"?`)) {
      this.loading = true;
      this.indexJournalService.deleteJournal(journal.journalKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadJournals(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete journal';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingJournal = null;
    this.journalForm.reset({ published: false });
  }
}

