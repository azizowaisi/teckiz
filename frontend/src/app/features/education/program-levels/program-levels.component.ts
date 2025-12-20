import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProgramService } from '../../../core/services/program.service';
import { ProgramLevelResponse, ProgramLevelRequest } from '../../../core/models/program.model';

@Component({
  selector: 'app-program-levels',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="program-levels-container">
      <div class="header">
        <h1>Program Levels Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Level</button>
      </div>

      <div *ngIf="showCreateForm || editingLevel" class="form-container">
        <h2>{{ editingLevel ? 'Edit Program Level' : 'Create New Program Level' }}</h2>
        <form [formGroup]="levelForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="levelForm.get('title')?.invalid && levelForm.get('title')?.touched" class="error">
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
            <label>Program Level Type ID</label>
            <input type="number" formControlName="programLevelTypeId" />
          </div>

          <div class="form-group">
            <label>
              <input type="checkbox" formControlName="published" />
              Published
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="levelForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingLevel ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading program levels...</div>

      <div class="levels-list" *ngIf="levels.length > 0">
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
            <tr *ngFor="let level of levels">
              <td>{{ level.title }}</td>
              <td>{{ level.slug }}</td>
              <td>
                <span [class]="level.published ? 'badge published' : 'badge draft'">
                  {{ level.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>{{ level.updatedAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editLevel(level)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteLevel(level)">Delete</button>
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
    .program-levels-container {
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

    .levels-list {
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
export class ProgramLevelsComponent implements OnInit {
  levels: ProgramLevelResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingLevel: ProgramLevelResponse | null = null;
  levelForm: FormGroup;
  currentPage = 0;
  totalPages = 0;

  constructor(
    private programService: ProgramService,
    private fb: FormBuilder
  ) {
    this.levelForm = this.fb.group({
      title: ['', Validators.required],
      slug: [''],
      shortDescription: [''],
      description: [''],
      thumbnail: [''],
      programLevelTypeId: [null],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadLevels();
  }

  loadLevels(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.programService.listProgramLevels(page, 20).subscribe({
      next: (response) => {
        this.levels = response.programLevels || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load program levels';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadLevels(page);
  }

  onSubmit(): void {
    if (this.levelForm.valid) {
      this.loading = true;
      const request: ProgramLevelRequest = this.levelForm.value;

      if (this.editingLevel) {
        this.programService.updateProgramLevel(this.editingLevel.levelKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadLevels(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update program level';
            this.loading = false;
          }
        });
      } else {
        this.programService.createProgramLevel(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadLevels();
          },
          error: () => {
            this.error = 'Failed to create program level';
            this.loading = false;
          }
        });
      }
    }
  }

  editLevel(level: ProgramLevelResponse): void {
    this.editingLevel = level;
    this.levelForm.patchValue({
      title: level.title,
      slug: level.slug,
      shortDescription: level.shortDescription || '',
      description: level.description || '',
      thumbnail: level.thumbnail || '',
      programLevelTypeId: level.programLevelTypeId || null,
      published: level.published
    });
    this.showCreateForm = true;
  }

  deleteLevel(level: ProgramLevelResponse): void {
    if (confirm(`Delete "${level.title}"?`)) {
      this.loading = true;
      this.programService.deleteProgramLevel(level.levelKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadLevels(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete program level';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingLevel = null;
    this.levelForm.reset({ published: false });
  }
}

