import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CompanyService } from '../../../core/services/company.service';
import { UserService } from '../../../core/services/user.service';
import { ModuleService } from '../../../core/services/module.service';

@Component({
  selector: 'app-super-admin-index',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="dashboard-overview">
      <h1>Super Admin Dashboard</h1>
      
      <div class="stats-grid">
        <div class="stat-card">
          <h3>Total Companies</h3>
          <p class="stat-number">{{ stats.companiesCount || 0 }}</p>
          <a routerLink="/superadmin/companies">Manage Companies</a>
        </div>
        <div class="stat-card">
          <h3>Total Users</h3>
          <p class="stat-number">{{ stats.usersCount || 0 }}</p>
          <a routerLink="/superadmin/users">Manage Users</a>
        </div>
        <div class="stat-card">
          <h3>Total Modules</h3>
          <p class="stat-number">{{ stats.modulesCount || 0 }}</p>
          <a routerLink="/superadmin/modules">Manage Modules</a>
        </div>
      </div>

      <div class="quick-actions">
        <h2>Quick Actions</h2>
        <div class="actions-grid">
          <a routerLink="/superadmin/companies" class="action-card">
            <h3>Create New Company</h3>
            <p>Add a new company to the system</p>
          </a>
          <a routerLink="/superadmin/users" class="action-card">
            <h3>View All Users</h3>
            <p>Manage system users</p>
          </a>
          <a routerLink="/superadmin/modules" class="action-card">
            <h3>Manage Modules</h3>
            <p>Configure available modules</p>
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-overview {
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
  `]
})
export class SuperAdminIndexComponent implements OnInit {
  stats: any = {
    companiesCount: 0,
    usersCount: 0,
    modulesCount: 0
  };

  constructor(
    private companyService: CompanyService,
    private userService: UserService,
    private moduleService: ModuleService
  ) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.companyService.getAllCompanies().subscribe({
      next: (response) => {
        this.stats.companiesCount = response.companies?.length || 0;
      }
    });

    this.userService.getAllUsers().subscribe({
      next: (response) => {
        this.stats.usersCount = response.users?.length || 0;
      }
    });

    this.moduleService.getAllModules().subscribe({
      next: (response) => {
        this.stats.modulesCount = response.modules?.length || 0;
      }
    });
  }
}
