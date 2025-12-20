import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Module } from '../models/module.model';

@Injectable({
  providedIn: 'root'
})
export class ModuleService {
  private apiUrl = `${environment.apiUrl}/superadmin/modules`;

  constructor(private http: HttpClient) {}

  getAllModules(): Observable<{ modules: Module[] }> {
    return this.http.get<{ modules: Module[] }>(this.apiUrl);
  }
}

