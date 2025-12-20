import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebContactTypeRequest, WebContactTypeResponse } from '../models/webcontacttype.model';

@Injectable({
  providedIn: 'root'
})
export class WebContactTypeService {
  private apiUrl = `${environment.apiUrl}/website/admin/contact-types`;

  constructor(private http: HttpClient) {}

  listContactTypes(): Observable<{ contactTypes: WebContactTypeResponse[] }> {
    return this.http.get<{ contactTypes: WebContactTypeResponse[] }>(this.apiUrl);
  }

  getContactType(typeKey: string): Observable<WebContactTypeResponse> {
    return this.http.get<WebContactTypeResponse>(`${this.apiUrl}/${typeKey}`);
  }

  createContactType(request: WebContactTypeRequest): Observable<{ message: string; typeKey: string }> {
    return this.http.post<{ message: string; typeKey: string }>(this.apiUrl, request);
  }

  updateContactType(typeKey: string, request: WebContactTypeRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${typeKey}`, request);
  }

  deleteContactType(typeKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${typeKey}`);
  }
}

