import { Injectable, inject } from '@angular/core';
import { 
  HttpRequest, 
  HttpHandler, 
  HttpEvent, 
  HttpInterceptor, 
  HttpErrorResponse 
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../auth/auth.service';
import { ToastService } from '../services/toast.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private router = inject(Router);


  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    console.log('Interceptor called for:', request.url);
    
    const token = this.authService.getToken();
    console.log('Token exists:', !!token);

    let authRequest = request;
    if (token) {
      authRequest = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('Added auth header to request');
    }
    
    return next.handle(authRequest).pipe(
      catchError((error: HttpErrorResponse) => {
        console.log('HTTP Error:', error.status, error.message);
        
        // Handle different HTTP error statuses with toast notifications
        switch (error.status) {
          case 401:
            console.log('401 error - logging out');
            this.toastService.error('Your session has expired. Please log in again.');
            this.authService.logout();
            this.router.navigate(['/auth/login']);
            break;
            
          case 403:
            this.toastService.error('You do not have permission to perform this action.');
            break;
            
          case 404:
            this.toastService.error('The requested resource was not found.');
            break;
            
          case 500:
            this.toastService.error('Internal server error. Please try again later.');
            break;
            
          case 0:
            // Network error or server is down
            this.toastService.error('Unable to connect to server. Please check your internet connection.');
            break;
            
          default:
            // For other errors, show a generic message or the server's error message
            const errorMessage = error.error?.message || error.message || 'An unexpected error occurred.';
            this.toastService.error(errorMessage);
            break;
        }
        
        return throwError(() => error);
      })
    );
  } 
}