import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StoryRequest, StoryResponse } from '../models/story.model';

@Injectable({
  providedIn: 'root'
})
export class StoryService {
  private apiUrl = `${environment.apiUrl}/education/admin/stories`;

  constructor(private http: HttpClient) {}

  listStories(page: number = 0, size: number = 20, published?: boolean, storyTypeId?: number): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    if (storyTypeId) {
      params = params.set('storyTypeId', storyTypeId.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getStory(storyKey: string): Observable<StoryResponse> {
    return this.http.get<StoryResponse>(`${this.apiUrl}/${storyKey}`);
  }

  createStory(request: StoryRequest): Observable<{ message: string; storyKey: string }> {
    return this.http.post<{ message: string; storyKey: string }>(this.apiUrl, request);
  }

  updateStory(storyKey: string, request: StoryRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${storyKey}`, request);
  }

  deleteStory(storyKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${storyKey}`);
  }
}

