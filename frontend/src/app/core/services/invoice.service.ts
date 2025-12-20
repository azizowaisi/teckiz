import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CompanyInvoiceRequest } from '../models/invoice.model';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private apiUrl = `${environment.apiUrl}/superadmin/invoices`;

  constructor(private http: HttpClient) {}

  listInvoices(page: number = 0, size: number = 20, companyKey?: string, status?: string): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (companyKey) {
      params = params.set('companyKey', companyKey);
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getInvoice(invoiceKey: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${invoiceKey}`);
  }

  getCompanyInvoices(companyKey: string, page: number = 0, size: number = 20, status?: string): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<any>(`${this.apiUrl}/company/${companyKey}`, { params });
  }

  createInvoice(request: CompanyInvoiceRequest): Observable<{ message: string; invoiceKey: string }> {
    return this.http.post<{ message: string; invoiceKey: string }>(this.apiUrl, request);
  }

  updateInvoice(invoiceKey: string, request: Partial<CompanyInvoiceRequest>): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${invoiceKey}`, request);
  }

  deleteInvoice(invoiceKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${invoiceKey}`);
  }
}

