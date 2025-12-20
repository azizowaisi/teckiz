import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProgramTermService } from '../../../core/services/programterm.service';
import { ProgramService } from '../../../core/services/program.service';
import { ProgramTermResponse, ProgramTermRequest } from '../../../core/models/programterm.model';
import { ProgramLevelResponse } from '../../../core/models/program.model';

@Component({
  selector: 'app-program-terms',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="program-terms-container">
      <div class="header">
        <h1>Program Terms Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Term</button>
      </div>

      <div class="filters">
        <label>Filter by Program Level:</label>
        <select [(ngModel)]="selectedLevelId" (change)="loadTerms()">
          <option [value]="null">All Levels</option>
          <option *ngFor="let level of programLevels" [value]="level.id">
            {{ level.title }}
          </option>
        </select>
      </div>

      <div *ngIf="showCreateForm || editingTerm" class="form-container">
        <h2>{{ editingTerm ? 'Edit Program Term' : 'Create New Program Term' }}</h2>
        <form [formGroup]="termForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Program Level *</label>
            <select formControlName="programLevelId">
              <option value="">Select a level</option>
              <option *ngFor="let level of programLevels" [value]="level.id">
                {{ level.title }}
              </option>
            </select>
            <div *ngIf="termForm.get('programLevelId')?.invalid && termForm.get('programLevelId')?.touched" class="error">
              Program level is required
            </div>
          </div>

          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="termForm.get('title')?.invalid && termForm.get('title')?.touched" class="error">
              Title is required
            </div>
          </div>

          <div class="form-group">
            <label>Slug</label>
            <input type="text" formControlName="slug" />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Start Date</label>
              <input type="date" formControlName="startDate" />
            </div>
            <div class="form-group">
              <label>End Date</label>
              <input type="date" formControlName="endDate" />
            </div>
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
            <button type="submit" class="btn btn-primary" [disabled]="termForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingTerm ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading program terms...</div>

      <div class="terms-list" *ngIf="terms.length > 0">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Program Level</th>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Status</th>
              <th>Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let term of terms">
              <td>{{ term.title }}</td>
              <td>{{ getLevelName(term.programLevelId) }}</td>
              <td>{{ term.startDate | date:'short' }}</td>
              <td>{{ term.endDate | date:'short' }}</td>
              <td>
                <span [class]="term.published ? 'badge published' : 'badge draft'">
                  {{ term.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>{{ term.updatedAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editTerm(term)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteTerm(term)">Delete</button>
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
    .program-terms-container {
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

    .terms-list {
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
export class ProgramTermsComponent implements OnInit {
  terms: ProgramTermResponse[] = [];
  programLevels: ProgramLevelResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingTerm: ProgramTermResponse | null = null;
  termForm: FormGroup;
  currentPage = 0;
  totalPages = 0;
  selectedLevelId: number | null = null;

  constructor(
    private programTermService: ProgramTermService,
    private programService: ProgramService,
    private fb: FormBuilder
  ) {
    this.termForm = this.fb.group({
      programLevelId: ['', Validators.required],
      title: ['', Validators.required],
      slug: [''],
      shortDescription: [''],
      description: [''],
      startDate: [''],
      endDate: [''],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadProgramLevels();
    this.loadTerms();
  }

  loadProgramLevels(): void {
    this.programService.listProgramLevels(0, 100).subscribe({
      next: (response) => {
        this.programLevels = response.programLevels || [];
      }
    });
  }

  loadTerms(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.programTermService.listProgramTerms(page, 20, this.selectedLevelId || undefined).subscribe({
      next: (response) => {
        this.terms = response.programTerms || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load program terms';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadTerms(page);
  }

  getLevelName(levelId: number): string {
    const level = this.programLevels.find(l => l.id === levelId);
    return level ? level.title : 'Unknown';
  }

  onSubmit(): void {
    if (this.termForm.valid) {
      this.loading = true;
      const request: ProgramTermRequest = this.termForm.value;

      if (this.editingTerm) {
        this.programTermService.updateProgramTerm(this.editingTerm.termKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadTerms(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update program term';
            this.loading = false;
          }
        });
      } else {
        this.programTermService.createProgramTerm(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadTerms();
          },
          error: () => {
            this.error = 'Failed to create program term';
            this.loading = false;
          }
        });
      }
    }
  }

  editTerm(term: ProgramTermResponse): void {
    this.editingTerm = term;
    this.termForm.patchValue({
      programLevelId: term.programLevelId,
      title: term.title,
      slug: term.slug,
      shortDescription: term.shortDescription || '',
      description: term.description || '',
      startDate: term.startDate ? new Date(term.startDate).toISOString().split('T')[0] : '',
      endDate: term.endDate ? new Date(term.endDate).toISOString().split('T')[0] : '',
      published: term.published
    });
    this.showCreateForm = true;
  }

  deleteTerm(term: ProgramTermResponse): void {
    if (confirm(`Delete "${term.title}"?`)) {
      this.loading = true;
      this.programTermService.deleteProgramTerm(term.termKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadTerms(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete program term';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingTerm = null;
    this.termForm.reset({ published: false });
  }
}

