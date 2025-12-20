import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProgramTermRequest, ProgramTermResponse } from '../models/programterm.model';

@Injectable({
  providedIn: 'root'
})
export class ProgramTermService {
  private apiUrl = `${environment.apiUrl}/education/admin/program-terms`;

  constructor(private http: HttpClient) {}

  listProgramTerms(page: number = 0, size: number = 20, programLevelId?: number, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (programLevelId) {
      params = params.set('programLevelId', programLevelId.toString());
    }
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getProgramTerm(termKey: string): Observable<ProgramTermResponse> {
    return this.http.get<ProgramTermResponse>(`${this.apiUrl}/${termKey}`);
  }

  createProgramTerm(request: ProgramTermRequest): Observable<{ message: string; termKey: string }> {
    return this.http.post<{ message: string; termKey: string }>(this.apiUrl, request);
  }

  updateProgramTerm(termKey: string, request: ProgramTermRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${termKey}`, request);
  }

  deleteProgramTerm(termKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${termKey}`);
  }
}

