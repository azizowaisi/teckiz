import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { WebEventService } from '../../../core/services/webevent.service';
import { WebEventResponse, WebEventRequest } from '../../../core/models/webevent.model';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="events-container">
      <div class="header">
        <h1>Events Management</h1>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Event</button>
      </div>

      <div *ngIf="showCreateForm || editingEvent" class="form-container">
        <h2>{{ editingEvent ? 'Edit Event' : 'Create New Event' }}</h2>
        <form [formGroup]="eventForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="eventForm.get('title')?.invalid && eventForm.get('title')?.touched" class="error">
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

          <div class="form-row">
            <div class="form-group">
              <label>Start Date</label>
              <input type="datetime-local" formControlName="startDate" />
            </div>
            <div class="form-group">
              <label>End Date</label>
              <input type="datetime-local" formControlName="endDate" />
            </div>
          </div>

          <div class="form-group">
            <label>Location</label>
            <input type="text" formControlName="location" />
          </div>

          <div class="form-group">
            <label>
              <input type="checkbox" formControlName="published" />
              Published
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="eventForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingEvent ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading events...</div>

      <div class="events-list" *ngIf="events.length > 0">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Start Date</th>
              <th>End Date</th>
              <th>Location</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let event of events">
              <td>{{ event.title }}</td>
              <td>{{ event.startDate | date:'short' }}</td>
              <td>{{ event.endDate | date:'short' }}</td>
              <td>{{ event.location || '-' }}</td>
              <td>
                <span [class]="event.published ? 'badge published' : 'badge draft'">
                  {{ event.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editEvent(event)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteEvent(event)">Delete</button>
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
    .events-container {
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

    .events-list {
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
export class EventsComponent implements OnInit {
  events: WebEventResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingEvent: WebEventResponse | null = null;
  eventForm: FormGroup;
  currentPage = 0;
  totalPages = 0;

  constructor(
    private webEventService: WebEventService,
    private fb: FormBuilder
  ) {
    this.eventForm = this.fb.group({
      title: ['', Validators.required],
      slug: [''],
      shortDescription: [''],
      description: [''],
      startDate: [''],
      endDate: [''],
      location: [''],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.webEventService.listEvents(page, 20).subscribe({
      next: (response) => {
        this.events = response.events || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load events';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadEvents(page);
  }

  onSubmit(): void {
    if (this.eventForm.valid) {
      this.loading = true;
      const request: WebEventRequest = this.eventForm.value;

      if (this.editingEvent) {
        this.webEventService.updateEvent(this.editingEvent.eventKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadEvents(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update event';
            this.loading = false;
          }
        });
      } else {
        this.webEventService.createEvent(request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadEvents();
          },
          error: () => {
            this.error = 'Failed to create event';
            this.loading = false;
          }
        });
      }
    }
  }

  editEvent(event: WebEventResponse): void {
    this.editingEvent = event;
    this.eventForm.patchValue({
      title: event.title,
      slug: event.slug,
      shortDescription: event.shortDescription || '',
      description: event.description || '',
      startDate: event.startDate ? new Date(event.startDate).toISOString().slice(0, 16) : '',
      endDate: event.endDate ? new Date(event.endDate).toISOString().slice(0, 16) : '',
      location: event.location || '',
      published: event.published
    });
    this.showCreateForm = true;
  }

  deleteEvent(event: WebEventResponse): void {
    if (confirm(`Delete "${event.title}"?`)) {
      this.loading = true;
      this.webEventService.deleteEvent(event.eventKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadEvents(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete event';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingEvent = null;
    this.eventForm.reset({ published: false });
  }
}

