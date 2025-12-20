import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="show" class="loading-spinner" [class.overlay]="overlay" [class.small]="size === 'small'" [class.medium]="size === 'medium'">
      <div class="spinner"></div>
      <p *ngIf="message" class="loading-message">{{ message }}</p>
    </div>
  `,
  styles: [`
    .loading-spinner {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20px;
    }

    .loading-spinner.overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.9);
      z-index: 9998;
    }

    .spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #007bff;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      animation: spin 1s linear infinite;
    }

    .loading-spinner.small .spinner {
      width: 20px;
      height: 20px;
      border-width: 2px;
    }

    .loading-spinner.medium .spinner {
      width: 30px;
      height: 30px;
      border-width: 3px;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .loading-message {
      margin-top: 10px;
      color: #6c757d;
      font-size: 14px;
    }
  `]
})
export class LoadingSpinnerComponent {
  @Input() show = false;
  @Input() overlay = false;
  @Input() size: 'small' | 'medium' | 'large' = 'large';
  @Input() message?: string;
}

