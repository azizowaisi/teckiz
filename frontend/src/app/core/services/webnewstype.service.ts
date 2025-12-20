import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebNewsTypeRequest, WebNewsTypeResponse } from '../models/webnewstype.model';

@Injectable({
  providedIn: 'root'
})
export class WebNewsTypeService {
  private apiUrl = `${environment.apiUrl}/website/admin/news-types`;

  constructor(private http: HttpClient) {}

  listNewsTypes(): Observable<{ newsTypes: WebNewsTypeResponse[] }> {
    return this.http.get<{ newsTypes: WebNewsTypeResponse[] }>(this.apiUrl);
  }

  getNewsType(typeKey: string): Observable<WebNewsTypeResponse> {
    return this.http.get<WebNewsTypeResponse>(`${this.apiUrl}/${typeKey}`);
  }

  createNewsType(request: WebNewsTypeRequest): Observable<{ message: string; typeKey: string }> {
    return this.http.post<{ message: string; typeKey: string }>(this.apiUrl, request);
  }

  updateNewsType(typeKey: string, request: WebNewsTypeRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${typeKey}`, request);
  }

  deleteNewsType(typeKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${typeKey}`);
  }
}

