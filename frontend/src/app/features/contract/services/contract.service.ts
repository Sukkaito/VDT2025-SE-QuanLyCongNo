import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Contract } from "../../../shared/models/contract.model";
import { BaseCrudService } from "../../../core/services/base-crud.service";
import { PagignatedResponse } from '../../../core/models/api-response.model';
import { Invoice } from '../../../shared/models/invoice.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ContractService extends BaseCrudService<Contract> {
    protected override http: HttpClient;

    protected endpoint = '/contracts'; // No need to include /api prefix as it's in the environment config

    constructor() {
        const http = inject(HttpClient);

        super(http);
    
        this.http = http;
    }

    override search(params?: any): Observable<PagignatedResponse<Contract>> {
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

        return this.http.get<PagignatedResponse<Contract>>(`${this.getFullUrl()}/search`, { params: httpParams });
    }

    getInvoicesByContractId(contractId: string, params?: any) {
        let httpParams = new HttpParams();
        if (params) {
            Object.keys(params).forEach(key => {
                if (params[key] !== null && params[key] !== undefined) {
                    httpParams = httpParams.set(key, params[key]);
                }
            });
        }

        return this.http.get<PagignatedResponse<Invoice>>(
            `${this.getFullUrl()}/${contractId}/invoices`,
            { params: httpParams }
        );
    }
}