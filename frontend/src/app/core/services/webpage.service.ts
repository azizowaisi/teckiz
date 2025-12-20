import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebPageRequest, WebPageResponse } from '../models/webpage.model';

@Injectable({
  providedIn: 'root'
})
export class WebPageService {
  private apiUrl = `${environment.apiUrl}/website/admin/pages`;

  constructor(private http: HttpClient) {}

  listPages(page: number = 0, size: number = 20, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getPage(pageKey: string): Observable<WebPageResponse> {
    return this.http.get<WebPageResponse>(`${this.apiUrl}/${pageKey}`);
  }

  createPage(request: WebPageRequest): Observable<{ message: string; pageKey: string }> {
    return this.http.post<{ message: string; pageKey: string }>(this.apiUrl, request);
  }

  updatePage(pageKey: string, request: WebPageRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${pageKey}`, request);
  }

  deletePage(pageKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${pageKey}`);
  }
}

