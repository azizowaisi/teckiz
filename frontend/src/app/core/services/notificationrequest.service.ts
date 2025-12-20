import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotificationRequestRequest } from '../models/notificationrequest.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationRequestService {
  private apiUrl = `${environment.apiUrl}/superadmin/notification-requests`;

  constructor(private http: HttpClient) {}

  listRequests(page: number = 0, size: number = 20, companyKey?: string, status?: string, targetType?: string): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (companyKey) {
      params = params.set('companyKey', companyKey);
    }
    if (status) {
      params = params.set('status', status);
    }
    if (targetType) {
      params = params.set('targetType', targetType);
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getRequest(requestKey: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${requestKey}`);
  }

  getPendingRequests(companyKey?: string): Observable<any> {
    let params = new HttpParams();
    if (companyKey) {
      params = params.set('companyKey', companyKey);
    }
    return this.http.get<any>(`${this.apiUrl}/pending`, { params });
  }

  createRequest(request: NotificationRequestRequest): Observable<{ message: string; requestKey: string }> {
    return this.http.post<{ message: string; requestKey: string }>(this.apiUrl, request);
  }

  updateRequest(requestKey: string, request: Partial<NotificationRequestRequest>): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${requestKey}`, request);
  }

  deleteRequest(requestKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${requestKey}`);
  }
}

