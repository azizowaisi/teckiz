import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebEventRequest, WebEventResponse } from '../models/webevent.model';

@Injectable({
  providedIn: 'root'
})
export class WebEventService {
  private apiUrl = `${environment.apiUrl}/website/admin/events`;

  constructor(private http: HttpClient) {}

  listEvents(page: number = 0, size: number = 20, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getEvent(eventKey: string): Observable<WebEventResponse> {
    return this.http.get<WebEventResponse>(`${this.apiUrl}/${eventKey}`);
  }

  createEvent(request: WebEventRequest): Observable<{ message: string; eventKey: string }> {
    return this.http.post<{ message: string; eventKey: string }>(this.apiUrl, request);
  }

  updateEvent(eventKey: string, request: WebEventRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${eventKey}`, request);
  }

  deleteEvent(eventKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${eventKey}`);
  }
}

