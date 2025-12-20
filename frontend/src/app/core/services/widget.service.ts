import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebWidgetRequest, WebWidgetResponse, WidgetContentRequest, WidgetContentResponse } from '../models/widget.model';

@Injectable({
  providedIn: 'root'
})
export class WidgetService {
  private apiUrl = `${environment.apiUrl}/website/admin/widgets`;

  constructor(private http: HttpClient) {}

  listWidgets(page: number = 0, size: number = 20, published?: boolean): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (published !== undefined) {
      params = params.set('published', published.toString());
    }
    return this.http.get<any>(this.apiUrl, { params });
  }

  getWidget(widgetKey: string): Observable<WebWidgetResponse> {
    return this.http.get<WebWidgetResponse>(`${this.apiUrl}/${widgetKey}`);
  }

  createWidget(request: WebWidgetRequest): Observable<{ message: string; widgetKey: string }> {
    return this.http.post<{ message: string; widgetKey: string }>(this.apiUrl, request);
  }

  updateWidget(widgetKey: string, request: WebWidgetRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/${widgetKey}`, request);
  }

  deleteWidget(widgetKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${widgetKey}`);
  }

  // Widget Content
  listWidgetContents(widgetKey: string, page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/${widgetKey}/contents`, { params });
  }

  getWidgetContent(contentKey: string): Observable<WidgetContentResponse> {
    return this.http.get<WidgetContentResponse>(`${this.apiUrl}/contents/${contentKey}`);
  }

  createWidgetContent(widgetKey: string, request: WidgetContentRequest): Observable<{ message: string; contentKey: string }> {
    return this.http.post<{ message: string; contentKey: string }>(`${this.apiUrl}/${widgetKey}/contents`, request);
  }

  updateWidgetContent(contentKey: string, request: WidgetContentRequest): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/contents/${contentKey}`, request);
  }

  deleteWidgetContent(contentKey: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/contents/${contentKey}`);
  }
}

