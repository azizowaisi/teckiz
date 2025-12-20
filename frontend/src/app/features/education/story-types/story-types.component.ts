import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { StoryTypeService } from '../../../core/services/storytype.service';
import { StoryTypeResponse, StoryTypeRequest } from '../../../core/models/storytype.model';

@Component({
  selector: 'app-story-types',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="story-types-container">
      <div class="header">
        <h1>Story Types Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Type</button>
      </div>

      <div *ngIf="showCreateForm || editingType" class="form-container">
        <h2>{{ editingType ? 'Edit Story Type' : 'Create New Story Type' }}</h2>
        <form [formGroup]="typeForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Name *</label>
            <input type="text" formControlName="name" />
            <div *ngIf="typeForm.get('name')?.invalid && typeForm.get('name')?.touched" class="error">
              Name is required
            </div>
          </div>

          <div class="form-group">
            <label>Description</label>
            <textarea formControlName="description" rows="3"></textarea>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="typeForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingType ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading story types...</div>

      <div class="types-list" *ngIf="types.length > 0">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Description</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let type of types">
              <td>{{ type.name }}</td>
              <td>{{ type.description || '-' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editType(type)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteType(type)">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .story-types-container {
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

    .types-list {
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
export class StoryTypesComponent implements OnInit {
  types: StoryTypeResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingType: StoryTypeResponse | null = null;
  typeForm: FormGroup;

  constructor(
    private storyTypeService: StoryTypeService,
    private fb: FormBuilder
  ) {
    this.typeForm = this.fb.group({
      name: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadTypes();
  }

  loadTypes(): void {
    this.loading = true;
    this.error = '';
    this.storyTypeService.listStoryTypes().subscribe({
      next: (response) => {
        this.types = response.storyTypes || [];
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load story types';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.typeForm.valid) {
      this.loading = true;
      const request: StoryTypeRequest = this.typeForm.value;

      if (this.editingType && this.editingType.typeKey) {
        this.storyTypeService.updateStoryType(this.editingType.typeKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadTypes();
          },
          error: () => {
            this.error = 'Failed to update story type';
            this.loading = false;
          }
        });
      } else {
        this.storyTypeService.createStoryType(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadTypes();
          },
          error: () => {
            this.error = 'Failed to create story type';
            this.loading = false;
          }
        });
      }
    }
  }

  editType(type: StoryTypeResponse): void {
    this.editingType = type;
    this.typeForm.patchValue({
      name: type.name,
      description: type.description || ''
    });
    this.showCreateForm = true;
  }

  deleteType(type: StoryTypeResponse): void {
    if (confirm(`Delete "${type.name}"?`) && type.typeKey) {
      this.loading = true;
      this.storyTypeService.deleteStoryType(type.typeKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadTypes();
        },
        error: () => {
          this.error = 'Failed to delete story type';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingType = null;
    this.typeForm.reset();
  }
}

