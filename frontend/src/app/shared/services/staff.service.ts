import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BaseCrudService } from '../../core/services/base-crud.service';
import { Staff } from '../models/staff.model';
import { PagignatedResponse } from '../../core/models/api-response.model';


@Injectable({
  providedIn: 'root'
})
export class StaffService extends BaseCrudService<Staff> {
    protected override http: HttpClient;

    protected endpoint = '/staff'; // No need to include /api prefix as it's in the environment config

    constructor() {
        const http = inject(HttpClient);

        super(http);
    
        this.http = http;
    }

    override search(params?: any): Observable<PagignatedResponse<Staff>> {
        let httpParams = new HttpParams();
        
        // Always set query parameter, use empty string if search not provided
        const query = params?.search || '';
        httpParams = httpParams.set('query', query);
        
        if (params) {
            Object.keys(params).forEach(key => {
                if (key !== 'search' && params[key] !== null && params[key] !== undefined && params[key] !== '') {
                    httpParams = httpParams.set(key, params[key]);
                }
            });
        }

        return this.http.get<PagignatedResponse<Staff>>(`${this.getFullUrl()}/search`, { params: httpParams });
    }
}