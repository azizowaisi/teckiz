import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProgramService } from '../../../core/services/program.service';
import { ProgramClassResponse, ProgramClassRequest, ProgramCourseResponse } from '../../../core/models/program.model';

@Component({
  selector: 'app-program-classes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="program-classes-container">
      <div class="header">
        <h1>Program Classes Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Class</button>
      </div>

      <div class="filters">
        <label>Filter by Program Course:</label>
        <select [(ngModel)]="selectedCourseId" (change)="loadClasses()">
          <option [value]="null">All Courses</option>
          <option *ngFor="let course of programCourses" [value]="course.id">
            {{ course.title }}
          </option>
        </select>
      </div>

      <div *ngIf="showCreateForm || editingClass" class="form-container">
        <h2>{{ editingClass ? 'Edit Program Class' : 'Create New Program Class' }}</h2>
        <form [formGroup]="classForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Program Course *</label>
            <select formControlName="programCourseId">
              <option value="">Select a course</option>
              <option *ngFor="let course of programCourses" [value]="course.id">
                {{ course.title }}
              </option>
            </select>
            <div *ngIf="classForm.get('programCourseId')?.invalid && classForm.get('programCourseId')?.touched" class="error">
              Program course is required
            </div>
          </div>

          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="classForm.get('title')?.invalid && classForm.get('title')?.touched" class="error">
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
            <label>
              <input type="checkbox" formControlName="published" />
              Published
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="classForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingClass ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading program classes...</div>

      <div class="classes-list" *ngIf="classes.length > 0">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Program Course</th>
              <th>Slug</th>
              <th>Status</th>
              <th>Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let classItem of classes">
              <td>{{ classItem.title }}</td>
              <td>{{ getCourseName(classItem.programCourseId) }}</td>
              <td>{{ classItem.slug }}</td>
              <td>
                <span [class]="classItem.published ? 'badge published' : 'badge draft'">
                  {{ classItem.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>{{ classItem.updatedAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editClass(classItem)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteClass(classItem)">Delete</button>
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
    .program-classes-container {
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

    .classes-list {
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
export class ProgramClassesComponent implements OnInit {
  classes: ProgramClassResponse[] = [];
  programCourses: ProgramCourseResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingClass: ProgramClassResponse | null = null;
  classForm: FormGroup;
  currentPage = 0;
  totalPages = 0;
  selectedCourseId: number | null = null;

  constructor(
    private programService: ProgramService,
    private fb: FormBuilder
  ) {
    this.classForm = this.fb.group({
      programCourseId: ['', Validators.required],
      title: ['', Validators.required],
      slug: [''],
      shortDescription: [''],
      description: [''],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadProgramCourses();
    this.loadClasses();
  }

  loadProgramCourses(): void {
    this.programService.listProgramCourses(0, 100).subscribe({
      next: (response) => {
        this.programCourses = response.programCourses || [];
      }
    });
  }

  loadClasses(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.programService.listProgramClasses(page, 20, this.selectedCourseId || undefined).subscribe({
      next: (response) => {
        this.classes = response.programClasses || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load program classes';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadClasses(page);
  }

  getCourseName(courseId: number): string {
    const course = this.programCourses.find(c => c.id === courseId);
    return course ? course.title : 'Unknown';
  }

  onSubmit(): void {
    if (this.classForm.valid) {
      this.loading = true;
      const request: ProgramClassRequest = this.classForm.value;

      if (this.editingClass) {
        this.programService.updateProgramClass(this.editingClass.classKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadClasses(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update program class';
            this.loading = false;
          }
        });
      } else {
        this.programService.createProgramClass(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadClasses();
          },
          error: () => {
            this.error = 'Failed to create program class';
            this.loading = false;
          }
        });
      }
    }
  }

  editClass(classItem: ProgramClassResponse): void {
    this.editingClass = classItem;
    this.classForm.patchValue({
      programCourseId: classItem.programCourseId,
      title: classItem.title,
      slug: classItem.slug,
      shortDescription: classItem.shortDescription || '',
      description: classItem.description || '',
      published: classItem.published
    });
    this.showCreateForm = true;
  }

  deleteClass(classItem: ProgramClassResponse): void {
    if (confirm(`Delete "${classItem.title}"?`)) {
      this.loading = true;
      this.programService.deleteProgramClass(classItem.classKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadClasses(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete program class';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingClass = null;
    this.classForm.reset({ published: false });
  }
}

