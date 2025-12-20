import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ResearchArticleRequest, ResearchArticleResponse } from '../models/researcharticle.model';

@Injectable({
  providedIn: 'root'
})
export class ResearchArticleService {
  private apiUrl = `${environment.apiUrl}/journal/admin/articles`;

  constructor(private http: HttpClient) {}

  listArticles(page: number = 0, size: number = 20, journalId?: number, volumeId?: number, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (journalId) {
      params = params.set('journalId', journalId.toString());
    }
    if (volumeId) {
      params = params.set('volumeId', volumeId.toString());
    }
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getArticle(articleKey: string): Observable<ResearchArticleResponse> {
    return this.http.get<ResearchArticleResponse>(`${this.apiUrl}/${articleKey}`);
  }

  createArticle(request: ResearchArticleRequest): Observable<{ message: string; articleKey: string }> {
    return this.http.post<{ message: string; articleKey: string }>(this.apiUrl, request);
  }

  updateArticle(articleKey: string, request: ResearchArticleRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${articleKey}`, request);
  }

  deleteArticle(articleKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${articleKey}`);
  }
}

