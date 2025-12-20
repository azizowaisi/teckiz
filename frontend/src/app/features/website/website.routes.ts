import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';

export const websiteRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./dashboard/website-dashboard.component').then(m => m.WebsiteDashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'pages',
    loadComponent: () => import('./pages/pages.component').then(m => m.PagesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'news',
    loadComponent: () => import('./news/news.component').then(m => m.NewsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'events',
    loadComponent: () => import('./events/events.component').then(m => m.EventsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'notifications',
    loadComponent: () => import('./notifications/notifications.component').then(m => m.NotificationsComponent),
    canActivate: [authGuard]
  }
];

