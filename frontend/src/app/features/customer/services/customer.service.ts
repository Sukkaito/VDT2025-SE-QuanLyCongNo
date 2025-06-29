import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BaseCrudService } from "../../../core/services/base-crud.service";
import { Customer } from '../../../shared/models/customer.model';
import { PagignatedResponse } from '../../../core/models/api-response.model';
import { Invoice } from '../../../shared/models/invoice.model';
import { Observable } from 'rxjs';
import { Contract } from '../../../shared/models/contract.model';

@Injectable({
  providedIn: 'root'
})
export class CustomerService extends BaseCrudService<Customer> {
    protected override http: HttpClient;

    protected endpoint = '/customers'; // No need to include /api prefix as it's in the environment config

    constructor() {
        const http = inject(HttpClient);

        super(http);
    
        this.http = http;
    }

    override search(params?: any): Observable<PagignatedResponse<Customer>> {
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

        return this.http.get<PagignatedResponse<Customer>>(`${this.getFullUrl()}/search`, { params: httpParams });
    }

    getInvoicesByCustomerId(customerId: string, params?: any): Observable<PagignatedResponse<Invoice>> {
        let httpParams = new HttpParams();
        if (params) {
            Object.keys(params).forEach(key => {
                if (params[key] !== null && params[key] !== undefined) {
                    httpParams = httpParams.set(key, params[key]);
                }
            });
        }

        return this.http.get<PagignatedResponse<Invoice>>(
            `${this.getFullUrl()}/${customerId}/invoices`,
            { params: httpParams }
        );
    }

    getContractByCustomerId(customerId: string, params?: any): Observable<PagignatedResponse<Contract>> {
        let httpParams = new HttpParams();
        if (params) {
            Object.keys(params).forEach(key => {
                if (params[key] !== null && params[key] !== undefined) {
                    httpParams = httpParams.set(key, params[key]);
                }
            });
        }

        return this.http.get<PagignatedResponse<Contract>>(
            `${this.getFullUrl()}/${customerId}/contracts`,
            { params: httpParams }
        );
    }
}