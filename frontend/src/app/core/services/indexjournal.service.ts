import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { IndexJournalRequest, IndexJournalResponse, IndexJournalVolumeRequest, IndexJournalVolumeResponse, IndexJournalArticleRequest, IndexJournalArticleResponse } from '../models/indexjournal.model';

@Injectable({
  providedIn: 'root'
})
export class IndexJournalService {
  private baseUrl = `${environment.apiUrl}/journal/admin/index-journals`;

  constructor(private http: HttpClient) {}

  // Index Journals
  listJournals(page: number = 0, size: number = 20, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(this.baseUrl, { params });
  }

  getJournal(journalKey: string): Observable<IndexJournalResponse> {
    return this.http.get<IndexJournalResponse>(`${this.baseUrl}/${journalKey}`);
  }

  createJournal(request: IndexJournalRequest): Observable<{ message: string; journalKey: string }> {
    return this.http.post<{ message: string; journalKey: string }>(this.baseUrl, request);
  }

  updateJournal(journalKey: string, request: IndexJournalRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/${journalKey}`, request);
  }

  deleteJournal(journalKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/${journalKey}`);
  }

  // Index Journal Volumes
  listVolumes(journalKey: string, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.baseUrl}/${journalKey}/volumes`, { params });
  }

  getVolume(volumeKey: string): Observable<IndexJournalVolumeResponse> {
    return this.http.get<IndexJournalVolumeResponse>(`${this.baseUrl}/volumes/${volumeKey}`);
  }

  createVolume(journalKey: string, request: IndexJournalVolumeRequest): Observable<{ message: string; volumeKey: string }> {
    return this.http.post<{ message: string; volumeKey: string }>(`${this.baseUrl}/${journalKey}/volumes`, request);
  }

  updateVolume(volumeKey: string, request: IndexJournalVolumeRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/volumes/${volumeKey}`, request);
  }

  deleteVolume(volumeKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/volumes/${volumeKey}`);
  }

  // Index Journal Articles
  listArticles(journalKey: string, page: number = 0, size: number = 20, volumeId?: number): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (volumeId) {
      params = params.set('volumeId', volumeId.toString());
    }
    return this.http.get<any>(`${this.baseUrl}/${journalKey}/articles`, { params });
  }

  getArticle(articleKey: string): Observable<IndexJournalArticleResponse> {
    return this.http.get<IndexJournalArticleResponse>(`${this.baseUrl}/articles/${articleKey}`);
  }

  createArticle(journalKey: string, request: IndexJournalArticleRequest): Observable<{ message: string; articleKey: string }> {
    return this.http.post<{ message: string; articleKey: string }>(`${this.baseUrl}/${journalKey}/articles`, request);
  }

  updateArticle(articleKey: string, request: IndexJournalArticleRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/articles/${articleKey}`, request);
  }

  deleteArticle(articleKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/articles/${articleKey}`);
  }
}

