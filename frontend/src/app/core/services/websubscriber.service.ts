import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebSubscriberRequest, WebSubscriberResponse } from '../models/websubscriber.model';

@Injectable({
  providedIn: 'root'
})
export class WebSubscriberService {
  private apiUrl = `${environment.apiUrl}/website/admin/subscribers`;

  constructor(private http: HttpClient) {}

  listSubscribers(page: number = 0, size: number = 20, verified?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (verified !== undefined) {
      params = params.set('verified', verified.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getSubscriber(subscriberKey: string): Observable<WebSubscriberResponse> {
    return this.http.get<WebSubscriberResponse>(`${this.apiUrl}/${subscriberKey}`);
  }

  createSubscriber(request: WebSubscriberRequest): Observable<{ message: string; subscriberKey: string }> {
    return this.http.post<{ message: string; subscriberKey: string }>(this.apiUrl, request);
  }

  deleteSubscriber(subscriberKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${subscriberKey}`);
  }

  sendVerificationEmail(subscriberKey: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/${subscriberKey}/send-verification`, {});
  }
}

