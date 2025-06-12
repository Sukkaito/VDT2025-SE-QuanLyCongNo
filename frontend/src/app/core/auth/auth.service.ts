import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { ApiResponse } from '../models/api-response.model';

interface LoginResponseData {
  accessToken: string;
  user: {
    id: string;
    username: string;
    role: string;
  };
}

interface LoginResponse extends ApiResponse<LoginResponseData | string> {
  data: LoginResponseData | string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API_URL = 'http://localhost:8080/api';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
  
  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/login`, { username, password })
      .pipe(
        tap(response => {
          let data: LoginResponseData = response.data as LoginResponseData;
          if (data instanceof String) {
            throw new Error(data.toString());
          }
          localStorage.setItem(this.TOKEN_KEY, data.accessToken);
          localStorage.setItem(this.USER_KEY, JSON.stringify(data.user));
          this.isAuthenticatedSubject.next(true);
        })
      );
  }
  
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/auth/login']);
  }
  
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
  
  getCurrentUser(): any {
    const userStr = localStorage.getItem(this.USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  }
  
  private hasToken(): boolean {
    return !!this.getToken();
  }

  isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }
}