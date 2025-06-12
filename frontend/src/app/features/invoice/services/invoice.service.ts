import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Invoice } from '../../../shared/models/invoice.model';
import { BaseCrudService } from "../../../core/services/base-crud.service";
import { PagignatedResponse } from '../../../core/models/api-response.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService extends BaseCrudService<Invoice> {
    protected override http: HttpClient;

    protected endpoint = '/invoices';

    constructor() {
        const http = inject(HttpClient);

        super(http);
        this.http = http;
    }
}