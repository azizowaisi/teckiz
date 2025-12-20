import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SkillRequest, SkillResponse } from '../models/skill.model';

@Injectable({
  providedIn: 'root'
})
export class SkillService {
  private apiUrl = `${environment.apiUrl}/education/admin/skills`;

  constructor(private http: HttpClient) {}

  listSkills(page: number = 0, size: number = 20, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getSkill(skillKey: string): Observable<SkillResponse> {
    return this.http.get<SkillResponse>(`${this.apiUrl}/${skillKey}`);
  }

  createSkill(request: SkillRequest): Observable<{ message: string; skillKey: string }> {
    return this.http.post<{ message: string; skillKey: string }>(this.apiUrl, request);
  }

  updateSkill(skillKey: string, request: SkillRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${skillKey}`, request);
  }

  deleteSkill(skillKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${skillKey}`);
  }
}

