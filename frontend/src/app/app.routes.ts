import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { SuperAdminDashboardComponent } from './features/super-admin/dashboard/super-admin-dashboard.component';
import { authGuard } from './core/guards/auth.guard';
import { superAdminGuard } from './core/guards/super-admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  {
    path: 'superadmin',
    component: SuperAdminDashboardComponent,
    canActivate: [authGuard, superAdminGuard],
    children: [
      { path: '', redirectTo: 'index', pathMatch: 'full' },
      { path: 'index', loadComponent: () => import('./features/super-admin/index/super-admin-index.component').then(m => m.SuperAdminIndexComponent) },
      { path: 'users', loadComponent: () => import('./features/super-admin/users/users.component').then(m => m.UsersComponent) },
      { path: 'modules', loadComponent: () => import('./features/super-admin/modules/modules.component').then(m => m.ModulesComponent) },
    ]
  },
  {
    path: 'website',
    loadChildren: () => import('./features/website/website.routes').then(m => m.websiteRoutes),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: '/login' }
];

