import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProgramLevelRequest, ProgramLevelResponse, ProgramCourseRequest, ProgramCourseResponse, ProgramClassRequest, ProgramClassResponse } from '../models/program.model';

@Injectable({
  providedIn: 'root'
})
export class ProgramService {
  private baseUrl = `${environment.apiUrl}/education/admin`;

  constructor(private http: HttpClient) {}

  // Program Levels
  listProgramLevels(page: number = 0, size: number = 20, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(`${this.baseUrl}/program-levels`, { params });
  }

  getProgramLevel(levelKey: string): Observable<ProgramLevelResponse> {
    return this.http.get<ProgramLevelResponse>(`${this.baseUrl}/program-levels/${levelKey}`);
  }

  createProgramLevel(request: ProgramLevelRequest): Observable<{ message: string; levelKey: string }> {
    return this.http.post<{ message: string; levelKey: string }>(`${this.baseUrl}/program-levels`, request);
  }

  updateProgramLevel(levelKey: string, request: ProgramLevelRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/program-levels/${levelKey}`, request);
  }

  deleteProgramLevel(levelKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/program-levels/${levelKey}`);
  }

  // Program Courses
  listProgramCourses(page: number = 0, size: number = 20, programLevelId?: number, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (programLevelId) {
      params = params.set('programLevelId', programLevelId.toString());
    }
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(`${this.baseUrl}/program-courses`, { params });
  }

  getProgramCourse(courseKey: string): Observable<ProgramCourseResponse> {
    return this.http.get<ProgramCourseResponse>(`${this.baseUrl}/program-courses/${courseKey}`);
  }

  createProgramCourse(request: ProgramCourseRequest): Observable<{ message: string; courseKey: string }> {
    return this.http.post<{ message: string; courseKey: string }>(`${this.baseUrl}/program-courses`, request);
  }

  updateProgramCourse(courseKey: string, request: ProgramCourseRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/program-courses/${courseKey}`, request);
  }

  deleteProgramCourse(courseKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/program-courses/${courseKey}`);
  }

  // Program Classes
  listProgramClasses(page: number = 0, size: number = 20, programCourseId?: number, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (programCourseId) {
      params = params.set('programCourseId', programCourseId.toString());
    }
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(`${this.baseUrl}/program-classes`, { params });
  }

  getProgramClass(classKey: string): Observable<ProgramClassResponse> {
    return this.http.get<ProgramClassResponse>(`${this.baseUrl}/program-classes/${classKey}`);
  }

  createProgramClass(request: ProgramClassRequest): Observable<{ message: string; classKey: string }> {
    return this.http.post<{ message: string; classKey: string }>(`${this.baseUrl}/program-classes`, request);
  }

  updateProgramClass(classKey: string, request: ProgramClassRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/program-classes/${classKey}`, request);
  }

  deleteProgramClass(classKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.baseUrl}/program-classes/${classKey}`);
  }
}

