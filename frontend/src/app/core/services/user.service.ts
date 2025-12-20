import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/superadmin/users`;

  constructor(private http: HttpClient) {}

  getAllUsers(search?: string): Observable<{ users: User[] }> {
    let params = new HttpParams();
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<{ users: User[] }>(this.apiUrl, { params });
  }
}

