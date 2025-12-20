import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebMediaService } from '../../../core/services/webmedia.service';
import { WebRelatedMediaResponse } from '../../../core/models/webmedia.model';

@Component({
  selector: 'app-media',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="media-container">
      <div class="header">
        <h1>Media Library</h1>
        <div>
          <input type="file" #fileInput (change)="onFileSelected($event)" style="display: none" accept="image/*,video/*,.pdf,.doc,.docx" />
          <button class="btn btn-primary" (click)="fileInput.click()">Upload File</button>
        </div>
      </div>

      <div *ngIf="uploading" class="upload-progress">
        <p>Uploading... {{ uploadProgress }}%</p>
        <div class="progress-bar">
          <div class="progress-fill" [style.width.%]="uploadProgress"></div>
        </div>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading && !uploading" class="loading">Loading media...</div>

      <div class="media-filters">
        <select [(ngModel)]="selectedFileType" (change)="loadMedia()">
          <option value="">All Types</option>
          <option value="image">Images</option>
          <option value="video">Videos</option>
          <option value="document">Documents</option>
        </select>
      </div>

      <div class="media-grid" *ngIf="media.length > 0">
        <div *ngFor="let item of media" class="media-item">
          <div class="media-preview" *ngIf="item.fileType?.startsWith('image')">
            <img [src]="item.filePath" [alt]="item.fileName" />
          </div>
          <div class="media-preview document" *ngIf="!item.fileType?.startsWith('image')">
            <span class="file-icon">📄</span>
          </div>
          <div class="media-info">
            <p class="media-name">{{ item.fileName }}</p>
            <p class="media-type">{{ item.fileType }}</p>
            <p class="media-size" *ngIf="item.fileSize">{{ formatFileSize(item.fileSize) }}</p>
          </div>
          <div class="media-actions">
            <button class="btn btn-sm btn-primary" (click)="copyUrl(item)">Copy URL</button>
            <button class="btn btn-sm btn-danger" (click)="deleteMedia(item)">Delete</button>
          </div>
        </div>
      </div>

      <div class="pagination" *ngIf="totalPages > 1">
        <button (click)="loadPage(currentPage - 1)" [disabled]="currentPage === 0">Previous</button>
        <span>Page {{ currentPage + 1 }} of {{ totalPages }}</span>
        <button (click)="loadPage(currentPage + 1)" [disabled]="currentPage >= totalPages - 1">Next</button>
      </div>
    </div>
  `,
  styles: [`
    .media-container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .upload-progress {
      background: #e3f2fd;
      padding: 15px;
      border-radius: 8px;
      margin-bottom: 20px;
    }

    .progress-bar {
      width: 100%;
      height: 8px;
      background: #ddd;
      border-radius: 4px;
      overflow: hidden;
      margin-top: 10px;
    }

    .progress-fill {
      height: 100%;
      background: #007bff;
      transition: width 0.3s;
    }

    .media-filters {
      margin-bottom: 20px;
    }

    .media-filters select {
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .media-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 20px;
    }

    .media-item {
      background: white;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .media-preview {
      width: 100%;
      height: 150px;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
    }

    .media-preview img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .media-preview.document {
      flex-direction: column;
    }

    .file-icon {
      font-size: 48px;
    }

    .media-info {
      padding: 10px;
    }

    .media-name {
      font-weight: 500;
      margin: 0 0 5px 0;
      word-break: break-word;
    }

    .media-type,
    .media-size {
      font-size: 12px;
      color: #666;
      margin: 2px 0;
    }

    .media-actions {
      padding: 10px;
      display: flex;
      gap: 5px;
      border-top: 1px solid #eee;
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

    .btn-danger {
      background-color: #dc3545;
      color: white;
    }

    .btn-sm {
      padding: 4px 8px;
      font-size: 12px;
      flex: 1;
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
export class MediaComponent implements OnInit {
  media: WebRelatedMediaResponse[] = [];
  loading = false;
  uploading = false;
  uploadProgress = 0;
  error = '';
  currentPage = 0;
  totalPages = 0;
  selectedFileType = '';

  constructor(private webMediaService: WebMediaService) {}

  ngOnInit(): void {
    this.loadMedia();
  }

  loadMedia(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.webMediaService.listMedia(page, 20, this.selectedFileType || undefined).subscribe({
      next: (response) => {
        this.media = response.media || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load media';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadMedia(page);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      this.uploadFile(file);
    }
  }

  uploadFile(file: File): void {
    this.uploading = true;
    this.uploadProgress = 0;
    this.error = '';

    this.webMediaService.uploadFile(file).subscribe({
      next: (event) => {
        if (event.type === 1) { // HttpEventType.UploadProgress
          this.uploadProgress = Math.round((event.loaded / (event.total || 1)) * 100);
        } else if (event.type === 4) { // HttpEventType.Response
          this.uploading = false;
          this.uploadProgress = 0;
          this.loadMedia(this.currentPage);
        }
      },
      error: () => {
        this.error = 'Failed to upload file';
        this.uploading = false;
        this.uploadProgress = 0;
      }
    });
  }

  copyUrl(media: WebRelatedMediaResponse): void {
    navigator.clipboard.writeText(media.filePath).then(() => {
      alert('URL copied to clipboard!');
    });
  }

  deleteMedia(media: WebRelatedMediaResponse): void {
    if (confirm(`Delete ${media.fileName}?`)) {
      this.loading = true;
      this.webMediaService.deleteMedia(media.mediaKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadMedia(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete media';
          this.loading = false;
        }
      });
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  }
}

