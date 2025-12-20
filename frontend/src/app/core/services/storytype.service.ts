import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StoryTypeRequest, StoryTypeResponse } from '../models/storytype.model';

@Injectable({
  providedIn: 'root'
})
export class StoryTypeService {
  private apiUrl = `${environment.apiUrl}/education/admin/story-types`;

  constructor(private http: HttpClient) {}

  listStoryTypes(): Observable<{ storyTypes: StoryTypeResponse[] }> {
    return this.http.get<{ storyTypes: StoryTypeResponse[] }>(this.apiUrl);
  }

  getStoryType(typeKey: string): Observable<StoryTypeResponse> {
    return this.http.get<StoryTypeResponse>(`${this.apiUrl}/${typeKey}`);
  }

  createStoryType(request: StoryTypeRequest): Observable<{ message: string; typeKey: string }> {
    return this.http.post<{ message: string; typeKey: string }>(this.apiUrl, request);
  }

  updateStoryType(typeKey: string, request: StoryTypeRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${typeKey}`, request);
  }

  deleteStoryType(typeKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${typeKey}`);
  }
}

