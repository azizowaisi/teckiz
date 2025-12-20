import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProgramService } from '../../../core/services/program.service';
import { ProgramCourseResponse, ProgramCourseRequest, ProgramLevelResponse } from '../../../core/models/program.model';

@Component({
  selector: 'app-program-courses',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="program-courses-container">
      <div class="header">
        <h1>Program Courses Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Course</button>
      </div>

      <div class="filters">
        <label>Filter by Program Level:</label>
        <select [(ngModel)]="selectedLevelId" (change)="loadCourses()">
          <option [value]="null">All Levels</option>
          <option *ngFor="let level of programLevels" [value]="level.id">
            {{ level.title }}
          </option>
        </select>
      </div>

      <div *ngIf="showCreateForm || editingCourse" class="form-container">
        <h2>{{ editingCourse ? 'Edit Program Course' : 'Create New Program Course' }}</h2>
        <form [formGroup]="courseForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Program Level *</label>
            <select formControlName="programLevelId">
              <option value="">Select a level</option>
              <option *ngFor="let level of programLevels" [value]="level.id">
                {{ level.title }}
              </option>
            </select>
            <div *ngIf="courseForm.get('programLevelId')?.invalid && courseForm.get('programLevelId')?.touched" class="error">
              Program level is required
            </div>
          </div>

          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="courseForm.get('title')?.invalid && courseForm.get('title')?.touched" class="error">
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
            <button type="submit" class="btn btn-primary" [disabled]="courseForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingCourse ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading program courses...</div>

      <div class="courses-list" *ngIf="courses.length > 0">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Program Level</th>
              <th>Slug</th>
              <th>Status</th>
              <th>Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let course of courses">
              <td>{{ course.title }}</td>
              <td>{{ getLevelName(course.programLevelId) }}</td>
              <td>{{ course.slug }}</td>
              <td>
                <span [class]="course.published ? 'badge published' : 'badge draft'">
                  {{ course.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>{{ course.updatedAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editCourse(course)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteCourse(course)">Delete</button>
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
    .program-courses-container {
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

    .courses-list {
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
export class ProgramCoursesComponent implements OnInit {
  courses: ProgramCourseResponse[] = [];
  programLevels: ProgramLevelResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingCourse: ProgramCourseResponse | null = null;
  courseForm: FormGroup;
  currentPage = 0;
  totalPages = 0;
  selectedLevelId: number | null = null;

  constructor(
    private programService: ProgramService,
    private fb: FormBuilder
  ) {
    this.courseForm = this.fb.group({
      programLevelId: ['', Validators.required],
      title: ['', Validators.required],
      slug: [''],
      shortDescription: [''],
      description: [''],
      thumbnail: [''],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadProgramLevels();
    this.loadCourses();
  }

  loadProgramLevels(): void {
    this.programService.listProgramLevels(0, 100).subscribe({
      next: (response) => {
        this.programLevels = response.programLevels || [];
      }
    });
  }

  loadCourses(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.programService.listProgramCourses(page, 20, this.selectedLevelId || undefined).subscribe({
      next: (response) => {
        this.courses = response.programCourses || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load program courses';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadCourses(page);
  }

  getLevelName(levelId: number): string {
    const level = this.programLevels.find(l => l.id === levelId);
    return level ? level.title : 'Unknown';
  }

  onSubmit(): void {
    if (this.courseForm.valid) {
      this.loading = true;
      const request: ProgramCourseRequest = this.courseForm.value;

      if (this.editingCourse) {
        this.programService.updateProgramCourse(this.editingCourse.courseKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadCourses(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update program course';
            this.loading = false;
          }
        });
      } else {
        this.programService.createProgramCourse(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadCourses();
          },
          error: () => {
            this.error = 'Failed to create program course';
            this.loading = false;
          }
        });
      }
    }
  }

  editCourse(course: ProgramCourseResponse): void {
    this.editingCourse = course;
    this.courseForm.patchValue({
      programLevelId: course.programLevelId,
      title: course.title,
      slug: course.slug,
      shortDescription: course.shortDescription || '',
      description: course.description || '',
      thumbnail: course.thumbnail || '',
      published: course.published
    });
    this.showCreateForm = true;
  }

  deleteCourse(course: ProgramCourseResponse): void {
    if (confirm(`Delete "${course.title}"?`)) {
      this.loading = true;
      this.programService.deleteProgramCourse(course.courseKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadCourses(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete program course';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingCourse = null;
    this.courseForm.reset({ published: false });
  }
}

