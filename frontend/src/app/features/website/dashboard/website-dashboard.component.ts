import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { WebsiteService } from '../../../core/services/website.service';

@Component({
  selector: 'app-website-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="website-dashboard">
      <h1>Website Dashboard</h1>
      
      <div *ngIf="loading" class="loading">Loading dashboard...</div>
      <div *ngIf="error" class="error-message">{{ error }}</div>

      <div *ngIf="stats" class="stats-grid">
        <div class="stat-card">
          <h3>Pages</h3>
          <p class="stat-number">{{ stats.pagesCount || 0 }}</p>
          <a routerLink="/website/pages">Manage Pages</a>
        </div>
        <div class="stat-card">
          <h3>News</h3>
          <p class="stat-number">{{ stats.newsCount || 0 }}</p>
          <a routerLink="/website/news">Manage News</a>
        </div>
        <div class="stat-card">
          <h3>Events</h3>
          <p class="stat-number">{{ stats.eventsCount || 0 }}</p>
          <a routerLink="/website/events">Manage Events</a>
        </div>
        <div class="stat-card">
          <h3>Albums</h3>
          <p class="stat-number">{{ stats.albumsCount || 0 }}</p>
          <a routerLink="/website/albums">Manage Albums</a>
        </div>
      </div>

      <div class="quick-actions">
        <h2>Quick Actions</h2>
        <div class="actions-grid">
          <a routerLink="/website/pages" class="action-card">
            <h3>Create New Page</h3>
            <p>Add a new page to your website</p>
          </a>
          <a routerLink="/website/news" class="action-card">
            <h3>Create News Article</h3>
            <p>Publish a new news article</p>
          </a>
          <a routerLink="/website/events" class="action-card">
            <h3>Create Event</h3>
            <p>Add a new event</p>
          </a>
          <a routerLink="/website/media" class="action-card">
            <h3>Media Library</h3>
            <p>Manage your media files</p>
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .website-dashboard {
      padding: 20px;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 20px;
      margin: 20px 0;
    }

    .stat-card {
      background: white;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .stat-card h3 {
      margin: 0 0 10px 0;
      color: #666;
      font-size: 14px;
      text-transform: uppercase;
    }

    .stat-number {
      font-size: 32px;
      font-weight: bold;
      margin: 10px 0;
      color: #333;
    }

    .stat-card a {
      color: #007bff;
      text-decoration: none;
      font-size: 14px;
    }

    .quick-actions {
      margin-top: 40px;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
      margin-top: 20px;
    }

    .action-card {
      background: white;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      text-decoration: none;
      color: inherit;
      transition: transform 0.2s, box-shadow 0.2s;
    }

    .action-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0,0,0,0.15);
    }

    .action-card h3 {
      margin: 0 0 10px 0;
      color: #333;
    }

    .action-card p {
      margin: 0;
      color: #666;
      font-size: 14px;
    }

    .error-message {
      background-color: #f8d7da;
      color: #721c24;
      padding: 12px;
      border-radius: 4px;
      margin: 20px 0;
    }

    .loading {
      text-align: center;
      padding: 20px;
    }
  `]
})
export class WebsiteDashboardComponent implements OnInit {
  stats: any = null;
  loading = false;
  error = '';

  constructor(private websiteService: WebsiteService) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.websiteService.getDashboardStats().subscribe({
      next: (response) => {
        this.stats = response;
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load dashboard statistics';
        this.loading = false;
      }
    });
  }
}
