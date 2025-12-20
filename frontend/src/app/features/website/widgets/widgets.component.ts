import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { WidgetService } from '../../../core/services/widget.service';
import { WebWidgetResponse, WebWidgetRequest, WidgetContentResponse, WidgetContentRequest } from '../../../core/models/widget.model';

@Component({
  selector: 'app-widgets',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="widgets-container">
      <div class="header">
        <h1>Widgets Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Widget</button>
      </div>

      <div *ngIf="showCreateForm || editingWidget" class="form-container">
        <h2>{{ editingWidget ? 'Edit Widget' : 'Create New Widget' }}</h2>
        <form [formGroup]="widgetForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="widgetForm.get('title')?.invalid && widgetForm.get('title')?.touched" class="error">
              Title is required
            </div>
          </div>

          <div class="form-group">
            <label>Widget Type *</label>
            <select formControlName="widgetType">
              <option value="">Select a type</option>
              <option value="text">Text</option>
              <option value="html">HTML</option>
              <option value="image">Image</option>
              <option value="video">Video</option>
              <option value="link">Link</option>
              <option value="custom">Custom</option>
            </select>
            <div *ngIf="widgetForm.get('widgetType')?.invalid && widgetForm.get('widgetType')?.touched" class="error">
              Widget type is required
            </div>
          </div>

          <div class="form-group">
            <label>Position</label>
            <input type="text" formControlName="position" placeholder="e.g., sidebar, footer, header" />
          </div>

          <div class="form-group">
            <label>Order</label>
            <input type="number" formControlName="order" />
          </div>

          <div class="form-group">
            <label>
              <input type="checkbox" formControlName="published" />
              Published
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="widgetForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingWidget ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading widgets...</div>

      <div class="widgets-list" *ngIf="widgets.length > 0">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Type</th>
              <th>Position</th>
              <th>Order</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let widget of widgets">
              <td>{{ widget.title }}</td>
              <td>{{ widget.widgetType }}</td>
              <td>{{ widget.position || '-' }}</td>
              <td>{{ widget.order || '-' }}</td>
              <td>
                <span [class]="widget.published ? 'badge published' : 'badge draft'">
                  {{ widget.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>
                <button class="btn btn-sm btn-info" (click)="manageContents(widget)">Contents</button>
                <button class="btn btn-sm btn-primary" (click)="editWidget(widget)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteWidget(widget)">Delete</button>
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

      <!-- Widget Contents Modal -->
      <div *ngIf="selectedWidget" class="modal-overlay" (click)="closeContentsModal()">
        <div class="modal-content large" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Widget Contents: {{ selectedWidget.title }}</h2>
            <button class="btn-close" (click)="closeContentsModal()">×</button>
          </div>
          <div class="modal-body">
            <button class="btn btn-primary" (click)="showContentForm = true">Add Content</button>

            <div *ngIf="showContentForm" class="form-container">
              <h3>{{ editingContent ? 'Edit Content' : 'Add New Content' }}</h3>
              <form [formGroup]="contentForm" (ngSubmit)="onContentSubmit()">
                <div class="form-group">
                  <label>Title</label>
                  <input type="text" formControlName="title" />
                </div>
                <div class="form-group">
                  <label>Content *</label>
                  <textarea formControlName="content" rows="6"></textarea>
                  <div *ngIf="contentForm.get('content')?.invalid && contentForm.get('content')?.touched" class="error">
                    Content is required
                  </div>
                </div>
                <div class="form-group">
                  <label>Order</label>
                  <input type="number" formControlName="order" />
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn btn-primary" [disabled]="contentForm.invalid || loading">
                    {{ loading ? 'Saving...' : (editingContent ? 'Update' : 'Create') }}
                  </button>
                  <button type="button" class="btn btn-secondary" (click)="cancelContentForm()">Cancel</button>
                </div>
              </form>
            </div>

            <div class="contents-list" *ngIf="contents.length > 0">
              <table>
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Content Preview</th>
                    <th>Order</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let content of contents">
                    <td>{{ content.title || '-' }}</td>
                    <td>{{ (content.content || '').substring(0, 50) }}...</td>
                    <td>{{ content.order || '-' }}</td>
                    <td>
                      <button class="btn btn-sm btn-primary" (click)="editContent(content)">Edit</button>
                      <button class="btn btn-sm btn-danger" (click)="deleteContent(content)">Delete</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .widgets-container {
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

    .widgets-list {
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

    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .modal-content {
      background: white;
      border-radius: 8px;
      max-width: 600px;
      width: 90%;
      max-height: 80vh;
      overflow-y: auto;
    }

    .modal-content.large {
      max-width: 900px;
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #ddd;
    }

    .btn-close {
      background: none;
      border: none;
      font-size: 24px;
      cursor: pointer;
      color: #999;
    }

    .modal-body {
      padding: 20px;
    }

    .contents-list {
      margin-top: 20px;
    }
  `]
})
export class WidgetsComponent implements OnInit {
  widgets: WebWidgetResponse[] = [];
  contents: WidgetContentResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingWidget: WebWidgetResponse | null = null;
  widgetForm: FormGroup;
  currentPage = 0;
  totalPages = 0;
  selectedWidget: WebWidgetResponse | null = null;
  showContentForm = false;
  editingContent: WidgetContentResponse | null = null;
  contentForm: FormGroup;

  constructor(
    private widgetService: WidgetService,
    private fb: FormBuilder
  ) {
    this.widgetForm = this.fb.group({
      title: ['', Validators.required],
      widgetType: ['', Validators.required],
      position: [''],
      order: [null],
      published: [false]
    });

    this.contentForm = this.fb.group({
      title: [''],
      content: ['', Validators.required],
      order: [null]
    });
  }

  ngOnInit(): void {
    this.loadWidgets();
  }

  loadWidgets(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.widgetService.listWidgets(page, 20).subscribe({
      next: (response) => {
        this.widgets = response.widgets || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load widgets';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadWidgets(page);
  }

  onSubmit(): void {
    if (this.widgetForm.valid) {
      this.loading = true;
      const request: WebWidgetRequest = this.widgetForm.value;

      if (this.editingWidget) {
        this.widgetService.updateWidget(this.editingWidget.widgetKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadWidgets(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update widget';
            this.loading = false;
          }
        });
      } else {
        this.widgetService.createWidget(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadWidgets();
          },
          error: () => {
            this.error = 'Failed to create widget';
            this.loading = false;
          }
        });
      }
    }
  }

  editWidget(widget: WebWidgetResponse): void {
    this.editingWidget = widget;
    this.widgetForm.patchValue({
      title: widget.title,
      widgetType: widget.widgetType,
      position: widget.position || '',
      order: widget.order || null,
      published: widget.published
    });
    this.showCreateForm = true;
  }

  deleteWidget(widget: WebWidgetResponse): void {
    if (confirm(`Delete "${widget.title}"?`)) {
      this.loading = true;
      this.widgetService.deleteWidget(widget.widgetKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadWidgets(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete widget';
          this.loading = false;
        }
      });
    }
  }

  manageContents(widget: WebWidgetResponse): void {
    this.selectedWidget = widget;
    this.loadContents(widget.widgetKey);
  }

  closeContentsModal(): void {
    this.selectedWidget = null;
    this.showContentForm = false;
    this.editingContent = null;
    this.contents = [];
  }

  loadContents(widgetKey: string): void {
    this.widgetService.listWidgetContents(widgetKey, 0, 100).subscribe({
      next: (response) => {
        this.contents = response.contents || [];
      }
    });
  }

  onContentSubmit(): void {
    if (this.contentForm.valid && this.selectedWidget) {
      this.loading = true;
      const request: WidgetContentRequest = {
        ...this.contentForm.value,
        widgetId: this.selectedWidget.id
      };

      if (this.editingContent) {
        this.widgetService.updateWidgetContent(this.editingContent.contentKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelContentForm();
            this.loadContents(this.selectedWidget!.widgetKey);
          },
          error: () => {
            this.error = 'Failed to update content';
            this.loading = false;
          }
        });
      } else {
        this.widgetService.createWidgetContent(this.selectedWidget.widgetKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelContentForm();
            this.loadContents(this.selectedWidget!.widgetKey);
          },
          error: () => {
            this.error = 'Failed to create content';
            this.loading = false;
          }
        });
      }
    }
  }

  editContent(content: WidgetContentResponse): void {
    this.editingContent = content;
    this.contentForm.patchValue({
      title: content.title || '',
      content: content.content,
      order: content.order || null
    });
    this.showContentForm = true;
  }

  deleteContent(content: WidgetContentResponse): void {
    if (confirm('Delete this content?')) {
      this.loading = true;
      this.widgetService.deleteWidgetContent(content.contentKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadContents(this.selectedWidget!.widgetKey);
        },
        error: () => {
          this.error = 'Failed to delete content';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingWidget = null;
    this.widgetForm.reset({ published: false });
  }

  cancelContentForm(): void {
    this.showContentForm = false;
    this.editingContent = null;
    this.contentForm.reset();
  }
}

