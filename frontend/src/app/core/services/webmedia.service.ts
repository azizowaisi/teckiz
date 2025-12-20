import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebRelatedMediaRequest, WebRelatedMediaResponse } from '../models/webmedia.model';

@Injectable({
  providedIn: 'root'
})
export class WebMediaService {
  private apiUrl = `${environment.apiUrl}/website/admin/media`;

  constructor(private http: HttpClient) {}

  listMedia(page: number = 0, size: number = 20, fileType?: string): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (fileType) {
      params = params.set('fileType', fileType);
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getMedia(mediaKey: string): Observable<WebRelatedMediaResponse> {
    return this.http.get<WebRelatedMediaResponse>(`${this.apiUrl}/${mediaKey}`);
  }

  uploadFile(file: File, description?: string): Observable<HttpEvent<any>> {
    const formData = new FormData();
    formData.append('file', file);
    if (description) {
      formData.append('description', description);
    }
    return this.http.post<any>(`${this.apiUrl}/upload`, formData, {
      reportProgress: true,
      observe: 'events'
    });
  }

  updateMedia(mediaKey: string, request: WebRelatedMediaRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${mediaKey}`, request);
  }

  deleteMedia(mediaKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${mediaKey}`);
  }
}

