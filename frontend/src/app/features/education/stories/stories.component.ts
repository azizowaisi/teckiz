import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { StoryService } from '../../../core/services/story.service';
import { StoryResponse, StoryRequest } from '../../../core/models/story.model';

@Component({
  selector: 'app-stories',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="stories-container">
      <div class="header">
        <h1>Stories Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Story</button>
      </div>

      <div *ngIf="showCreateForm || editingStory" class="form-container">
        <h2>{{ editingStory ? 'Edit Story' : 'Create New Story' }}</h2>
        <form [formGroup]="storyForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="storyForm.get('title')?.invalid && storyForm.get('title')?.touched" class="error">
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
            <label>Story Type ID</label>
            <input type="number" formControlName="storyTypeId" />
          </div>

          <div class="form-group">
            <label>
              <input type="checkbox" formControlName="published" />
              Published
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="storyForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingStory ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading stories...</div>

      <div class="stories-list" *ngIf="stories.length > 0">
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
            <tr *ngFor="let story of stories">
              <td>{{ story.title }}</td>
              <td>{{ story.slug }}</td>
              <td>
                <span [class]="story.published ? 'badge published' : 'badge draft'">
                  {{ story.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>{{ story.updatedAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editStory(story)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteStory(story)">Delete</button>
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
    .stories-container {
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

    .stories-list {
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
export class StoriesComponent implements OnInit {
  stories: StoryResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingStory: StoryResponse | null = null;
  storyForm: FormGroup;
  currentPage = 0;
  totalPages = 0;

  constructor(
    private storyService: StoryService,
    private fb: FormBuilder
  ) {
    this.storyForm = this.fb.group({
      title: ['', Validators.required],
      slug: [''],
      shortDescription: [''],
      description: [''],
      thumbnail: [''],
      storyTypeId: [null],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadStories();
  }

  loadStories(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.storyService.listStories(page, 20).subscribe({
      next: (response) => {
        this.stories = response.stories || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load stories';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadStories(page);
  }

  onSubmit(): void {
    if (this.storyForm.valid) {
      this.loading = true;
      const request: StoryRequest = this.storyForm.value;

      if (this.editingStory) {
        this.storyService.updateStory(this.editingStory.storyKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadStories(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update story';
            this.loading = false;
          }
        });
      } else {
        this.storyService.createStory(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadStories();
          },
          error: () => {
            this.error = 'Failed to create story';
            this.loading = false;
          }
        });
      }
    }
  }

  editStory(story: StoryResponse): void {
    this.editingStory = story;
    this.storyForm.patchValue({
      title: story.title,
      slug: story.slug,
      shortDescription: story.shortDescription || '',
      description: story.description || '',
      thumbnail: story.thumbnail || '',
      storyTypeId: story.storyTypeId || null,
      published: story.published
    });
    this.showCreateForm = true;
  }

  deleteStory(story: StoryResponse): void {
    if (confirm(`Delete "${story.title}"?`)) {
      this.loading = true;
      this.storyService.deleteStory(story.storyKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadStories(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete story';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingStory = null;
    this.storyForm.reset({ published: false });
  }
}

