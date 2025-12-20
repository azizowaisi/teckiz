import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ResearchJournalRequest, ResearchJournalResponse, ResearchJournalVolumeRequest, ResearchJournalVolumeResponse } from '../models/researchjournal.model';

@Injectable({
  providedIn: 'root'
})
export class ResearchJournalService {
  private baseUrl = `${environment.apiUrl}/journal/admin`;

  constructor(private http: HttpClient) {}

  // Research Journals
  listJournals(page: number = 0, size: number = 20, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(`${this.baseUrl}/journals`, { params });
  }

  getJournal(journalKey: string): Observable<ResearchJournalResponse> {
    return this.http.get<ResearchJournalResponse>(`${this.baseUrl}/journals/${journalKey}`);
  }

  createJournal(request: ResearchJournalRequest): Observable<{ message: string; journalKey: string }> {
    return this.http.post<{ message: string; journalKey: string }>(`${this.baseUrl}/journals`, request);
  }

  updateJournal(journalKey: string, request: ResearchJournalRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/journals/${journalKey}`, request);
  }

  deleteJournal(journalKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/journals/${journalKey}`);
  }

  // Research Journal Volumes
  listVolumes(journalKey: string, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.baseUrl}/journals/${journalKey}/volumes`, { params });
  }

  getVolume(volumeKey: string): Observable<ResearchJournalVolumeResponse> {
    return this.http.get<ResearchJournalVolumeResponse>(`${this.baseUrl}/volumes/${volumeKey}`);
  }

  createVolume(journalKey: string, request: ResearchJournalVolumeRequest): Observable<{ message: string; volumeKey: string }> {
    return this.http.post<{ message: string; volumeKey: string }>(`${this.baseUrl}/journals/${journalKey}/volumes`, request);
  }

  updateVolume(volumeKey: string, request: ResearchJournalVolumeRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/volumes/${volumeKey}`, request);
  }

  deleteVolume(volumeKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/volumes/${volumeKey}`);
  }
}

