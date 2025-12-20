import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';
import { LoadingService } from './core/services/loading.service';
import { LoadingSpinnerComponent } from './shared/components/loading-spinner/loading-spinner.component';
import { ToastComponent } from './shared/components/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, LoadingSpinnerComponent, ToastComponent],
  template: `
    <div class="app-container">
      <app-loading-spinner [show]="loading$ | async" [overlay]="true"></app-loading-spinner>
      <router-outlet></router-outlet>
      <app-toast></app-toast>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
    }
  `]
})
export class AppComponent {
  title = 'Teckiz';
  loading$: Observable<boolean>;

  constructor(private loadingService: LoadingService) {
    this.loading$ = this.loadingService.loading$;
  }
}

