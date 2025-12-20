import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WebContactService } from '../../../core/services/webcontact.service';
import { WebContactResponse } from '../../../core/models/webcontact.model';

@Component({
  selector: 'app-contacts',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="contacts-container">
      <div class="header">
        <h1>Contacts Management</h1>
      </div>

      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="loading" class="loading">Loading contacts...</div>

      <div class="contacts-list" *ngIf="contacts.length > 0">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Subject</th>
              <th>Received</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let contact of contacts">
              <td>{{ contact.name }}</td>
              <td>{{ contact.email || '-' }}</td>
              <td>{{ contact.phone || '-' }}</td>
              <td>{{ contact.subject || '-' }}</td>
              <td>{{ contact.createdAt | date:'short' }}</td>
              <td>
                <button class="btn btn-sm btn-primary" (click)="viewContact(contact)">View</button>
                <button class="btn btn-sm btn-danger" (click)="deleteContact(contact)">Delete</button>
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

      <div *ngIf="selectedContact" class="modal-overlay" (click)="closeModal()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Contact Details</h2>
            <button class="btn-close" (click)="closeModal()">×</button>
          </div>
          <div class="modal-body">
            <p><strong>Name:</strong> {{ selectedContact.name }}</p>
            <p><strong>Email:</strong> {{ selectedContact.email || '-' }}</p>
            <p><strong>Phone:</strong> {{ selectedContact.phone || '-' }}</p>
            <p><strong>Subject:</strong> {{ selectedContact.subject || '-' }}</p>
            <p><strong>Message:</strong></p>
            <div class="message-content">{{ selectedContact.message || '-' }}</div>
            <p><strong>Received:</strong> {{ selectedContact.createdAt | date:'full' }}</p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .contacts-container {
      padding: 20px;
    }

    .header {
      margin-bottom: 20px;
    }

    .contacts-list {
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

    .btn-danger {
      background-color: #dc3545;
      color: white;
    }

    .btn-sm {
      padding: 4px 8px;
      font-size: 12px;
      margin-right: 5px;
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

    .modal-body p {
      margin: 10px 0;
    }

    .message-content {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 4px;
      margin: 10px 0;
      white-space: pre-wrap;
    }
  `]
})
export class ContactsComponent implements OnInit {
  contacts: WebContactResponse[] = [];
  loading = false;
  error = '';
  currentPage = 0;
  totalPages = 0;
  selectedContact: WebContactResponse | null = null;

  constructor(private webContactService: WebContactService) {}

  ngOnInit(): void {
    this.loadContacts();
  }

  loadContacts(page: number = 0): void {
    this.loading = true;
    this.error = '';
    this.webContactService.listContacts(page, 20).subscribe({
      next: (response) => {
        this.contacts = response.contacts || [];
        this.currentPage = response.currentPage || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load contacts';
        this.loading = false;
      }
    });
  }

  loadPage(page: number): void {
    this.loadContacts(page);
  }

  viewContact(contact: WebContactResponse): void {
    this.selectedContact = contact;
  }

  closeModal(): void {
    this.selectedContact = null;
  }

  deleteContact(contact: WebContactResponse): void {
    if (confirm(`Delete contact from ${contact.name}?`)) {
      this.loading = true;
      this.webContactService.deleteContact(contact.contactKey).subscribe({
        next: () => {
          this.loading = false;
          this.loadContacts(this.currentPage);
        },
        error: () => {
          this.error = 'Failed to delete contact';
          this.loading = false;
        }
      });
    }
  }
}

