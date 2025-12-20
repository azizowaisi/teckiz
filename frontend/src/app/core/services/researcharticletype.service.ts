import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ResearchArticleTypeRequest, ResearchArticleTypeResponse } from '../models/researcharticletype.model';

@Injectable({
  providedIn: 'root'
})
export class ResearchArticleTypeService {
  private apiUrl = `${environment.apiUrl}/journal/admin/article-types`;

  constructor(private http: HttpClient) {}

  listArticleTypes(): Observable<{ articleTypes: ResearchArticleTypeResponse[] }> {
    return this.http.get<{ articleTypes: ResearchArticleTypeResponse[] }>(this.apiUrl);
  }

  getArticleType(typeKey: string): Observable<ResearchArticleTypeResponse> {
    return this.http.get<ResearchArticleTypeResponse>(`${this.apiUrl}/${typeKey}`);
  }

  createArticleType(request: ResearchArticleTypeRequest): Observable<{ message: string; typeKey: string }> {
    return this.http.post<{ message: string; typeKey: string }>(this.apiUrl, request);
  }

  updateArticleType(typeKey: string, request: ResearchArticleTypeRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${typeKey}`, request);
  }

  deleteArticleType(typeKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${typeKey}`);
  }
}

