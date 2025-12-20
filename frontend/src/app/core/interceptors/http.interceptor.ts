import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../../shared/services/toast.service';

export const httpInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  // Add auth token to request
  const token = authService.getToken();
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Unauthorized - clear token and redirect to login
        authService.logout();
        router.navigate(['/login']);
        toastService.error('Session expired. Please login again.');
      } else if (error.status === 403) {
        // Forbidden
        toastService.error('You do not have permission to perform this action.');
      } else if (error.status === 404) {
        // Not found
        toastService.error('Resource not found.');
      } else if (error.status >= 500) {
        // Server error
        toastService.error('Server error. Please try again later.');
      } else if (error.error?.message) {
        // Custom error message from backend
        toastService.error(error.error.message);
      } else {
        // Generic error
        toastService.error('An error occurred. Please try again.');
      }

      return throwError(() => error);
    })
  );
};

