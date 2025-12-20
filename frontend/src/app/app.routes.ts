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
        { path: 'companies', loadComponent: () => import('./features/super-admin/companies/companies.component').then(m => m.CompaniesComponent) },
        { path: 'companies/:companyKey/users', loadComponent: () => import('./features/super-admin/company-users/company-users.component').then(m => m.CompanyUsersComponent) },
        { path: 'users', loadComponent: () => import('./features/super-admin/users/users.component').then(m => m.UsersComponent) },
        { path: 'modules', loadComponent: () => import('./features/super-admin/modules/modules.component').then(m => m.ModulesComponent) },
      ]
  },
  {
    path: 'website',
    loadComponent: () => import('./features/website/layout/website-layout.component').then(m => m.WebsiteLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadChildren: () => import('./features/website/website.routes').then(m => m.websiteRoutes)
      }
    ]
  },
  {
    path: 'education',
    loadComponent: () => import('./features/website/layout/website-layout.component').then(m => m.WebsiteLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'facilities',
        loadComponent: () => import('./features/education/facilities/facilities.component').then(m => m.FacilitiesComponent)
      },
      {
        path: 'stories',
        loadComponent: () => import('./features/education/stories/stories.component').then(m => m.StoriesComponent)
      },
      {
        path: 'skills',
        loadComponent: () => import('./features/education/skills/skills.component').then(m => m.SkillsComponent)
      },
      {
        path: 'principal-message',
        loadComponent: () => import('./features/education/principal-message/principal-message.component').then(m => m.PrincipalMessageComponent)
      },
      {
        path: 'program-levels',
        loadComponent: () => import('./features/education/program-levels/program-levels.component').then(m => m.ProgramLevelsComponent)
      },
      {
        path: 'program-courses',
        loadComponent: () => import('./features/education/program-courses/program-courses.component').then(m => m.ProgramCoursesComponent)
      },
      {
        path: 'program-classes',
        loadComponent: () => import('./features/education/program-classes/program-classes.component').then(m => m.ProgramClassesComponent)
      }
    ]
  },
  {
    path: 'journal',
    loadComponent: () => import('./features/website/layout/website-layout.component').then(m => m.WebsiteLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'research-journals',
        loadComponent: () => import('./features/journal/research-journals/research-journals.component').then(m => m.ResearchJournalsComponent)
      },
      {
        path: 'research-articles',
        loadComponent: () => import('./features/journal/research-articles/research-articles.component').then(m => m.ResearchArticlesComponent)
      }
    ]
  },
  { path: '**', redirectTo: '/login' }
];

