import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ResearchArticleAuthorRequest, ResearchArticleAuthorResponse } from '../models/researcharticleauthor.model';

@Injectable({
  providedIn: 'root'
})
export class ResearchArticleAuthorService {
  private apiUrl = `${environment.apiUrl}/journal/admin/article-authors`;

  constructor(private http: HttpClient) {}

  listAuthors(articleKey: string): Observable<{ authors: ResearchArticleAuthorResponse[] }> {
    return this.http.get<{ authors: ResearchArticleAuthorResponse[] }>(`${this.apiUrl}/article/${articleKey}`);
  }

  getAuthor(authorKey: string): Observable<ResearchArticleAuthorResponse> {
    return this.http.get<ResearchArticleAuthorResponse>(`${this.apiUrl}/${authorKey}`);
  }

  createAuthor(articleKey: string, request: ResearchArticleAuthorRequest): Observable<{ message: string; authorKey: string }> {
    return this.http.post<{ message: string; authorKey: string }>(`${this.apiUrl}/article/${articleKey}`, request);
  }

  updateAuthor(authorKey: string, request: ResearchArticleAuthorRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${authorKey}`, request);
  }

  deleteAuthor(authorKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${authorKey}`);
  }
}

