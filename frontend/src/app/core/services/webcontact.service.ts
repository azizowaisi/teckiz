import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebContactRequest, WebContactResponse } from '../models/webcontact.model';

@Injectable({
  providedIn: 'root'
})
export class WebContactService {
  private apiUrl = `${environment.apiUrl}/website/admin/contacts`;

  constructor(private http: HttpClient) {}

  listContacts(page: number = 0, size: number = 20, contactTypeId?: number): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (contactTypeId) {
      params = params.set('contactTypeId', contactTypeId.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getContact(contactKey: string): Observable<WebContactResponse> {
    return this.http.get<WebContactResponse>(`${this.apiUrl}/${contactKey}`);
  }

  deleteContact(contactKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${contactKey}`);
  }
}

