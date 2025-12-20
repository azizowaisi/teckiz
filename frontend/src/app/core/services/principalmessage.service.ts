import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PrincipalMessageRequest, PrincipalMessageResponse } from '../models/principalmessage.model';

@Injectable({
  providedIn: 'root'
})
export class PrincipalMessageService {
  private apiUrl = `${environment.apiUrl}/education/admin/principal-message`;

  constructor(private http: HttpClient) {}

  getPrincipalMessage(): Observable<PrincipalMessageResponse> {
    return this.http.get<PrincipalMessageResponse>(this.apiUrl);
  }

  createPrincipalMessage(request: PrincipalMessageRequest): Observable<{ message: string; messageKey: string }> {
    return this.http.post<{ message: string; messageKey: string }>(this.apiUrl, request);
  }

  updatePrincipalMessage(request: PrincipalMessageRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(this.apiUrl, request);
  }

  deletePrincipalMessage(): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(this.apiUrl);
  }
}

