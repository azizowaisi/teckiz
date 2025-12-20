import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ResearchJournalService } from '../../../core/services/researchjournal.service';
import { ResearchJournalVolumeResponse, ResearchJournalVolumeRequest, ResearchJournalResponse } from '../../../core/models/researchjournal.model';

@Component({
  selector: 'app-journal-volumes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="volumes-container">
      <div class="header">
        <h1>Journal Volumes: {{ journalTitle }}</h1>
        <button class="btn btn-secondary" (click)="goBack()">Back to Journals</button>
        <button class="btn btn-primary" (click)="showCreateForm = true">Create New Volume</button>
      </div>

      <div *ngIf="showCreateForm || editingVolume" class="form-container">
        <h2>{{ editingVolume ? 'Edit Volume' : 'Create New Volume' }}</h2>
        <form [formGroup]="volumeForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="volumeForm.get('title')?.invalid && volumeForm.get('title')?.touched" class="error">
              Title is required
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Volume Number</label>
              <input type="number" formControlName="volumeNumber" />
            </div>
            <div class="form-group">
              <label>Year</label>
              <input type="number" formControlName="year" />
            </div>
          </div>

          <div class="form-group">
            <label>
              <input type="checkbox" formControlName="published" />
              Published
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="volumeForm.invalid || loading">
              {{ loading ? 'Saving...' : (editingVolume ? 'Update' : 'Create') }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="cancelForm()">Cancel</button>
          </div>
        </form>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !showCreateForm" class="loading">Loading volumes...</div>

      <div class="volumes-list" *ngIf="volumes.length > 0">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Volume #</th>
              <th>Year</th>
              <th>Status</th>
              <th>Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let volume of volumes">
              <td>{{ volume.title }}</td>
              <td>{{ volume.volumeNumber || '-' }}</td>
              <td>{{ volume.year || '-' }}</td>
              <td>
                <span [class]="volume.published ? 'badge published' : 'badge draft'">
                  {{ volume.published ? 'Published' : 'Draft' }}
                </span>
              </td>
              <td>{{ volume.updatedAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="editVolume(volume)">Edit</button>
                <button class="btn btn-sm btn-danger" (click)="deleteVolume(volume)">Delete</button>
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
    .volumes-container {
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

    .volumes-list {
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
export class JournalVolumesComponent implements OnInit {
  journalKey: string = '';
  journalTitle: string = '';
  volumes: ResearchJournalVolumeResponse[] = [];
  loading = false;
  error = '';
  showCreateForm = false;
  editingVolume: ResearchJournalVolumeResponse | null = null;
  volumeForm: FormGroup;
  currentPage = 0;
  totalPages = 0;

  constructor(
    private route: ActivatedRoute,
    private researchJournalService: ResearchJournalService,
    private fb: FormBuilder
  ) {
    this.volumeForm = this.fb.group({
      title: ['', Validators.required],
      volumeNumber: [null],
      year: [null],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.journalKey = this.route.snapshot.paramMap.get('journalKey') || '';
    if (this.journalKey) {
      this.loadJournal();
      this.loadVolumes();
    }
  }

  loadJournal(): void {
    this.researchJournalService.getJournal(this.journalKey).subscribe({
      next: (journal) => {
        this.journalTitle = journal.title;
      }
    });
  }

  loadVolumes(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.researchJournalService.listVolumes(this.journalKey, page, 20).subscribe({
      next: (response) => {
        this.volumes = response.volumes || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load volumes';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadVolumes(page);
  }

  onSubmit(): void {
    if (this.volumeForm.valid) {
      this.loading = true;
      const request: ResearchJournalVolumeRequest = {
        ...this.volumeForm.value,
        researchJournalId: 0 // Will be set by backend based on journalKey
      };

      if (this.editingVolume) {
        this.researchJournalService.updateVolume(this.editingVolume.volumeKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadVolumes(this.currentPage);
          },
          error: () => {
            this.error = 'Failed to update volume';
            this.loading = false;
          }
        });
      } else {
        this.researchJournalService.createVolume(this.journalKey, request).subscribe({
          next: () => {
            this.loading = false;
            this.cancelForm();
            this.loadVolumes();
          },
          error: () => {
            this.error = 'Failed to create volume';
            this.loading = false;
          }
        });
      }
    }
  }

  editVolume(volume: ResearchJournalVolumeResponse): void {
    this.editingVolume = volume;
    this.volumeForm.patchValue({
      title: volume.title,
      volumeNumber: volume.volumeNumber || null,
      year: volume.year || null,
      published: volume.published
    });
    this.showCreateForm = true;
  }

  deleteVolume(volume: ResearchJournalVolumeResponse): void {
    if (confirm(`Delete "${volume.title}"?`)) {
      this.loading = true;
      this.researchJournalService.deleteVolume(volume.volumeKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadVolumes(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete volume';
          this.loading = false;
        }
      });
    }
  }

  cancelForm(): void {
    this.showCreateForm = false;
    this.editingVolume = null;
    this.volumeForm.reset({ published: false });
  }

  goBack(): void {
    window.history.back();
  }
}

