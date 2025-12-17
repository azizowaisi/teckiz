import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';

export const websiteRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./dashboard/website-dashboard.component').then(m => m.WebsiteDashboardComponent),
    canActivate: [authGuard]
  }
];

