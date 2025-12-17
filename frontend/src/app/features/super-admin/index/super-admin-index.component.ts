import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-super-admin-index',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="index-container">
      <h1>Super Admin Dashboard</h1>
      <p>Welcome to the Teckiz Super Admin Panel</p>
    </div>
  `,
  styles: [`
    .index-container {
      padding: 20px;
    }
  `]
})
export class SuperAdminIndexComponent {
}

