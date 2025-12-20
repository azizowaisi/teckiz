import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebAlbumRequest, WebAlbumResponse } from '../models/webalbum.model';

@Injectable({
  providedIn: 'root'
})
export class WebAlbumService {
  private apiUrl = `${environment.apiUrl}/website/admin/albums`;

  constructor(private http: HttpClient) {}

  listAlbums(page: number = 0, size: number = 20, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getAlbum(albumKey: string): Observable<WebAlbumResponse> {
    return this.http.get<WebAlbumResponse>(`${this.apiUrl}/${albumKey}`);
  }

  createAlbum(request: WebAlbumRequest): Observable<{ message: string; albumKey: string }> {
    return this.http.post<{ message: string; albumKey: string }>(this.apiUrl, request);
  }

  updateAlbum(albumKey: string, request: WebAlbumRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${albumKey}`, request);
  }

  deleteAlbum(albumKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${albumKey}`);
  }
}

