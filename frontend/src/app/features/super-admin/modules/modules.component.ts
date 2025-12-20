import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

interface Module {
  id: number;
  name: string;
  description: string;
  active: boolean;
}

@Component({
  selector: 'app-modules',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="modules-container">
      <h1>Modules Management</h1>
      <div *ngIf="loading" class="loading">Loading modules...</div>
      <div *ngIf="error" class="error-message">{{ error }}</div>
      <div *ngIf="modules.length > 0" class="modules-grid">
        <div *ngFor="let module of modules" class="module-card">
          <h3>{{ module.name }}</h3>
          <p>{{ module.description }}</p>
          <span class="status" [class.active]="module.active">
            {{ module.active ? 'Active' : 'Inactive' }}
          </span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modules-container {
      padding: 20px;
    }

    .modules-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      margin-top: 20px;
    }

    .module-card {
      background: white;
      padding: 20px;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .module-card h3 {
      margin-bottom: 10px;
    }

    .module-card p {
      color: #666;
      margin-bottom: 15px;
    }

    .status {
      display: inline-block;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 12px;
      background-color: #dc3545;
      color: white;
    }

    .status.active {
      background-color: #28a745;
    }
  `]
})
export class ModulesComponent implements OnInit {
  modules: Module[] = [];
  loading = false;
  error = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadModules();
  }

  loadModules(): void {
    this.loading = true;
    this.http.get<{ modules: Module[] }>(`${environment.apiUrl}/superadmin/modules`).subscribe({
      next: (response) => {
        this.modules = response.modules || [];
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load modules';
        this.loading = false;
      }
    });
  }

  toggleModule(module: Module): void {
    // This would need backend support to toggle module active status
    console.log('Toggle module:', module);
  }
}

