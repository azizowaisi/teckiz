import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-website-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="website-dashboard">
      <h1>Website Dashboard</h1>
      <p>Welcome to the website management area</p>
    </div>
  `,
  styles: [`
    .website-dashboard {
      padding: 20px;
    }
  `]
})
export class WebsiteDashboardComponent {
}

