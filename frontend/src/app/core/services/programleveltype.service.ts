import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProgramLevelTypeRequest, ProgramLevelTypeResponse } from '../models/programleveltype.model';

@Injectable({
  providedIn: 'root'
})
export class ProgramLevelTypeService {
  private apiUrl = `${environment.apiUrl}/education/admin/program-level-types`;

  constructor(private http: HttpClient) {}

  listProgramLevelTypes(): Observable<{ programLevelTypes: ProgramLevelTypeResponse[] }> {
    return this.http.get<{ programLevelTypes: ProgramLevelTypeResponse[] }>(this.apiUrl);
  }

  getProgramLevelType(typeKey: string): Observable<ProgramLevelTypeResponse> {
    return this.http.get<ProgramLevelTypeResponse>(`${this.apiUrl}/${typeKey}`);
  }

  createProgramLevelType(request: ProgramLevelTypeRequest): Observable<{ message: string; typeKey: string }> {
    return this.http.post<{ message: string; typeKey: string }>(this.apiUrl, request);
  }

  updateProgramLevelType(typeKey: string, request: ProgramLevelTypeRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${typeKey}`, request);
  }

  deleteProgramLevelType(typeKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${typeKey}`);
  }
}

