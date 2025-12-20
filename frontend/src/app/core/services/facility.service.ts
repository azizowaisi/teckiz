import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FacilityRequest, FacilityResponse } from '../models/facility.model';

@Injectable({
  providedIn: 'root'
})
export class FacilityService {
  private apiUrl = `${environment.apiUrl}/education/admin/facilities`;

  constructor(private http: HttpClient) {}

  listFacilities(page: number = 0, size: number = 20, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getFacility(facilityKey: string): Observable<FacilityResponse> {
    return this.http.get<FacilityResponse>(`${this.apiUrl}/${facilityKey}`);
  }

  createFacility(request: FacilityRequest): Observable<{ message: string; facilityKey: string }> {
    return this.http.post<{ message: string; facilityKey: string }>(this.apiUrl, request);
  }

  updateFacility(facilityKey: string, request: FacilityRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${facilityKey}`, request);
  }

  deleteFacility(facilityKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${facilityKey}`);
  }
}

