import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/models/api-response.model';
import { StaffStatDTO } from '../../../shared/models/staff-stats.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class StaffStatsService {
  private http = inject(HttpClient);

  private readonly baseUrl = `${environment.apiUrl}/stats`;

  getTopStaffForMonth(limit = 10): Observable<ApiResponse<StaffStatDTO[]>> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<ApiResponse<StaffStatDTO[]>>(`${this.baseUrl}/staff/month`, { params });
  }

  getTopStaffAllTime(limit = 10): Observable<ApiResponse<StaffStatDTO[]>> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<ApiResponse<StaffStatDTO[]>>(`${this.baseUrl}/staff/all-time`, { params });
  }

  getTotalContractsForPeriod(startDate: string, endDate: string): Observable<ApiResponse<number>> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<ApiResponse<number>>(`${this.baseUrl}/contracts/period`, { params });
  }

  getTotalInvoicesForPeriod(startDate: string, endDate: string): Observable<ApiResponse<number>> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<ApiResponse<number>>(`${this.baseUrl}/invoices/period`, { params });
  }
}
