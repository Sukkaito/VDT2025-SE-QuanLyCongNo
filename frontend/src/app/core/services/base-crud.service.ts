import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PagignatedResponse } from '../models/api-response.model';
import { Router } from '@angular/router';

@Injectable()
export abstract class BaseCrudService<T> {
  protected abstract endpoint: string;
  protected baseUrl: string = environment.apiUrl;

  constructor(protected http: HttpClient) {}

  private router = inject(Router);

  /**
   * Get the full API URL by combining base URL and endpoint
   */
  protected getFullUrl(path: string = ''): string {
    return `${this.baseUrl}${this.endpoint}${path}`;
  }

  /**
   * Get all resources
   * @param params Optional HTTP parameters
   */
  getAll(params?: any): Observable<PagignatedResponse<T>> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    let httpHeaders = new HttpHeaders({ 'X-Action': 'getAll' });
        
    return this.http.get<PagignatedResponse<T>>(this.getFullUrl(), { params: httpParams, headers: httpHeaders });
  }

  /**
   * Get resource by ID
   * @param id Resource ID
   */
  getById(id: string | number): Observable<ApiResponse<T>> {
    return this.http.get<ApiResponse<T>>(this.getFullUrl(`/${id}`));
  }

  /**
   * Create a new resource
   * @param item Resource data
   */
  create(item: Partial<T>): Observable<ApiResponse<T>> {
    return this.http.post<ApiResponse<T>>(this.getFullUrl(), item);
  }

  /**
   * Update an existing resource
   * @param id Resource ID
   * @param item Updated resource data
   */
  update(id: string | number, item: Partial<T>): Observable<ApiResponse<T>> {
    return this.http.put<ApiResponse<T>>(this.getFullUrl(`/${id}`), item);
  }

  /**
   * Delete a resource
   * @param id Resource ID
   */
  delete(id: string | number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(this.getFullUrl(`/${id}`));
  }

  /**
   * Search resources by query parameters
   * @param queryParams Search parameters
   */
  search(queryParams: any): Observable<PagignatedResponse<T>> {
    let params = new HttpParams();
    
    Object.keys(queryParams).forEach(key => {
      if (queryParams[key] !== null && queryParams[key] !== undefined) {
        params = params.set(key, queryParams[key]);
      }
    });

    return this.http.get<PagignatedResponse<T>>(this.getFullUrl('/search'), { params });
  }

  /**
   * Import data from CSV file
   * @param file CSV file to import
   */
  import(file: File): Observable<ApiResponse<any>> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.http.post<ApiResponse<any>>(this.getFullUrl('/import'), formData, {
      headers: {
        'Accept': 'application/json'
      }
    });
  }

  /**
   * Export data to CSV file
   * @param params Optional export parameters
   */
  export(params?: any): Observable<Blob> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }

    return this.http.get(this.getFullUrl('/export'), { 
      params: httpParams,
      responseType: 'blob',
      headers: {
        'Accept': 'text/csv, application/csv'
      }
    });
  }
}
