import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebNewsRequest, WebNewsResponse } from '../models/webnews.model';

@Injectable({
  providedIn: 'root'
})
export class WebNewsService {
  private apiUrl = `${environment.apiUrl}/website/admin/news`;

  constructor(private http: HttpClient) {}

  listNews(page: number = 0, size: number = 20, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getNews(newsKey: string): Observable<WebNewsResponse> {
    return this.http.get<WebNewsResponse>(`${this.apiUrl}/${newsKey}`);
  }

  createNews(request: WebNewsRequest): Observable<{ message: string; newsKey: string }> {
    return this.http.post<{ message: string; newsKey: string }>(this.apiUrl, request);
  }

  updateNews(newsKey: string, request: WebNewsRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${newsKey}`, request);
  }

  deleteNews(newsKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${newsKey}`);
  }
}

