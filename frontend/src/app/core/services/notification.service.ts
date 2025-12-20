import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Notification, NotificationListResponse } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUnreadCount();
  }

  listNotifications(page: number = 0, size: number = 20, read?: boolean): Observable<NotificationListResponse> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (read !== undefined) {
      params = params.set('read', read.toString());
    }
    return this.http.get<NotificationListResponse>(this.apiUrl, { params }).pipe(
      tap(response => this.unreadCountSubject.next(response.unreadCount))
    );
  }

  getUnreadCount(): Observable<{ unreadCount: number }> {
    return this.http.get<{ unreadCount: number }>(`${this.apiUrl}/unread-count`).pipe(
      tap(response => this.unreadCountSubject.next(response.unreadCount))
    );
  }

  loadUnreadCount(): void {
    this.getUnreadCount().subscribe();
  }

  getNotification(notificationKey: string): Observable<Notification> {
    return this.http.get<Notification>(`${this.apiUrl}/${notificationKey}`);
  }

  markAsRead(notificationKey: string): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${notificationKey}/read`, {}).pipe(
      tap(() => this.loadUnreadCount())
    );
  }

  markAllAsRead(): Observable<{ message: string; count: number }> {
    return this.http.put<{ message: string; count: number }>(`${this.apiUrl}/read-all`, {}).pipe(
      tap(() => this.loadUnreadCount())
    );
  }

  deleteNotification(notificationKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${notificationKey}`).pipe(
      tap(() => this.loadUnreadCount())
    );
  }
}

