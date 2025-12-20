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
        { path: 'email-templates', loadComponent: () => import('./features/super-admin/email-templates/email-templates.component').then(m => m.EmailTemplatesComponent) },
        { path: 'invoices', loadComponent: () => import('./features/super-admin/invoices/invoices.component').then(m => m.InvoicesComponent) },
        { path: 'notification-requests', loadComponent: () => import('./features/super-admin/notification-requests/notification-requests.component').then(m => m.NotificationRequestsComponent) },
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
      },
      {
        path: 'program-terms',
        loadComponent: () => import('./features/education/program-terms/program-terms.component').then(m => m.ProgramTermsComponent)
      },
      {
        path: 'story-types',
        loadComponent: () => import('./features/education/story-types/story-types.component').then(m => m.StoryTypesComponent)
      },
      {
        path: 'program-level-types',
        loadComponent: () => import('./features/education/program-level-types/program-level-types.component').then(m => m.ProgramLevelTypesComponent)
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
      },
      {
        path: 'research-journals/:journalKey/volumes',
        loadComponent: () => import('./features/journal/journal-volumes/journal-volumes.component').then(m => m.JournalVolumesComponent)
      },
      {
        path: 'research-articles/:articleKey/authors',
        loadComponent: () => import('./features/journal/article-authors/article-authors.component').then(m => m.ArticleAuthorsComponent)
      },
      {
        path: 'article-types',
        loadComponent: () => import('./features/journal/article-types/article-types.component').then(m => m.ArticleTypesComponent)
      },
      {
        path: 'index-journals',
        loadComponent: () => import('./features/journal/index-journals/index-journals.component').then(m => m.IndexJournalsComponent)
      },
      {
        path: 'index-articles',
        loadComponent: () => import('./features/journal/index-articles/index-articles.component').then(m => m.IndexArticlesComponent)
      }
    ]
  },
  { path: '**', redirectTo: '/login' }
];

