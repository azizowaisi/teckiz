import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Company, CompanyRequest, CompanyResponse, AddUserToCompanyRequest, AddModuleToCompanyRequest } from '../models/company.model';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private apiUrl = `${environment.apiUrl}/superadmin/company`;

  constructor(private http: HttpClient) {}

  getAllCompanies(): Observable<{ companies: CompanyResponse[]; leftTab: string }> {
    return this.http.get<{ companies: CompanyResponse[]; leftTab: string }>(this.apiUrl);
  }

  getCompany(companyKey: string): Observable<CompanyResponse> {
    return this.http.get<CompanyResponse>(`${this.apiUrl}/${companyKey}`);
  }

  createCompany(request: CompanyRequest): Observable<{ message: string; companyKey: string }> {
    return this.http.post<{ message: string; companyKey: string }>(this.apiUrl, request);
  }

  updateCompany(companyKey: string, request: CompanyRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${companyKey}`, request);
  }

  deleteCompany(companyKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${companyKey}`);
  }

  getCompanyUsers(companyKey: string, page: number = 0, size: number = 20, search?: string): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<any>(`${this.apiUrl}/${companyKey}/users`, { params });
  }

  addUserToCompany(companyKey: string, request: AddUserToCompanyRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/${companyKey}/users`, request);
  }

  removeUserFromCompany(companyKey: string, userId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${companyKey}/users/${userId}`);
  }

  getCompanyModules(companyKey: string): Observable<{ modules: any[] }> {
    return this.http.get<{ modules: any[] }>(`${this.apiUrl}/${companyKey}/modules`);
  }

  addModuleToCompany(companyKey: string, request: AddModuleToCompanyRequest): Observable<{ message: string; moduleMapperKey: string }> {
    return this.http.post<{ message: string; moduleMapperKey: string }>(`${this.apiUrl}/${companyKey}/modules`, request);
  }

  removeModuleFromCompany(companyKey: string, moduleMapperKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${companyKey}/modules/${moduleMapperKey}`);
  }

  getCompanyRoles(companyKey: string): Observable<{ roles: any[] }> {
    return this.http.get<{ roles: any[] }>(`${this.apiUrl}/${companyKey}/roles`);
  }
}

