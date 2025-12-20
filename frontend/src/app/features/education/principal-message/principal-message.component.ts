import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PrincipalMessageService } from '../../../core/services/principalmessage.service';
import { PrincipalMessageResponse, PrincipalMessageRequest } from '../../../core/models/principalmessage.model';

@Component({
  selector: 'app-principal-message',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="principal-message-container">
      <div class="header">
        <h1>Principal Message Management</h1>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading" class="loading">Loading...</div>

      <div class="form-container" *ngIf="!loading">
        <form [formGroup]="messageForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Title *</label>
            <input type="text" formControlName="title" />
            <div *ngIf="messageForm.get('title')?.invalid && messageForm.get('title')?.touched" class="error">
              Title is required
            </div>
          </div>

          <div class="form-group">
            <label>Principal Name *</label>
            <input type="text" formControlName="principalName" />
            <div *ngIf="messageForm.get('principalName')?.invalid && messageForm.get('principalName')?.touched" class="error">
              Principal name is required
            </div>
          </div>

          <div class="form-group">
            <label>Message *</label>
            <textarea formControlName="message" rows="10"></textarea>
            <div *ngIf="messageForm.get('message')?.invalid && messageForm.get('message')?.touched" class="error">
              Message is required
            </div>
          </div>

          <div class="form-group">
            <label>Principal Image URL</label>
            <input type="text" formControlName="principalImage" />
          </div>

          <div class="form-group">
            <label>Signature URL</label>
            <input type="text" formControlName="signature" />
          </div>

          <div class="form-group">
            <label>
              <input type="checkbox" formControlName="published" />
              Published
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="messageForm.invalid || loading">
              {{ loading ? 'Saving...' : (existingMessage ? 'Update' : 'Create') }}
            </button>
            <button *ngIf="existingMessage" type="button" class="btn btn-danger" (click)="deleteMessage()">Delete</button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .principal-message-container {
      padding: 20px;
    }

    .header {
      margin-bottom: 20px;
    }

    .form-container {
      background: white;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      max-width: 800px;
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
export class PrincipalMessageComponent implements OnInit {
  existingMessage: PrincipalMessageResponse | null = null;
  loading = false;
  error = '';
  messageForm: FormGroup;

  constructor(
    private principalMessageService: PrincipalMessageService,
    private fb: FormBuilder
  ) {
    this.messageForm = this.fb.group({
      title: ['', Validators.required],
      principalName: ['', Validators.required],
      message: ['', Validators.required],
      principalImage: [''],
      signature: [''],
      published: [false]
    });
  }

  ngOnInit(): void {
    this.loadMessage();
  }

  loadMessage(): void {
    this.loading = true;
    this.principalMessageService.getPrincipalMessage().subscribe({
      next: (response) => {
        this.existingMessage = response;
        this.messageForm.patchValue({
          title: response.title,
          principalName: response.principalName,
          message: response.message,
          principalImage: response.principalImage || '',
          signature: response.signature || '',
          published: response.published
        });
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        // No message exists yet, form is ready for creation
      }
    });
  }

  onSubmit(): void {
    if (this.messageForm.valid) {
      this.loading = true;
      const request: PrincipalMessageRequest = this.messageForm.value;

      if (this.existingMessage) {
        this.principalMessageService.updatePrincipalMessage(request).subscribe({
          next: () => {
            this.loading = false;
            this.loadMessage();
          },
          error: () => {
            this.error = 'Failed to update principal message';
            this.loading = false;
          }
        });
      } else {
        this.principalMessageService.createPrincipalMessage(request).subscribe({
          next: () => {
            this.loading = false;
            this.loadMessage();
          },
          error: () => {
            this.error = 'Failed to create principal message';
            this.loading = false;
          }
        });
      }
    }
  }

  deleteMessage(): void {
    if (confirm('Delete principal message?')) {
      this.loading = true;
      this.principalMessageService.deletePrincipalMessage().subscribe({
        next: () => {
          this.loading = false;
          this.existingMessage = null;
          this.messageForm.reset({ published: false });
        },
        error: () => {
          this.error = 'Failed to delete principal message';
          this.loading = false;
        }
      });
    }
  }
}

