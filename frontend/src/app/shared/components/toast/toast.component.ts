import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { ToastService, Toast } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed top-4 right-4 z-[9999] space-y-2 pointer-events-none">
      <div
        *ngFor="let toast of toasts; trackBy: trackByToastId"
        [class]="getToastClasses(toast.type)"
        [class.animate-slide-in]="!toast.isRemoving"
        [class.animate-fade-out]="toast.isRemoving"
        class="toast-container min-w-80 max-w-md p-4 rounded-lg shadow-xl transform transition-all duration-300 ease-in-out pointer-events-auto"
        style="box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);"
      >
        <div class="flex items-start">
          <div class="flex-shrink-0">
            <!-- Success Icon -->
            <svg
              *ngIf="toast.type === 'success'"
              class="h-5 w-5 text-green-400"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fill-rule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clip-rule="evenodd"
              />
            </svg>
            
            <!-- Error Icon -->
            <svg
              *ngIf="toast.type === 'error'"
              class="h-5 w-5 text-red-400"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fill-rule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                clip-rule="evenodd"
              />
            </svg>
            
            <!-- Warning Icon -->
            <svg
              *ngIf="toast.type === 'warning'"
              class="h-5 w-5 text-yellow-400"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fill-rule="evenodd"
                d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                clip-rule="evenodd"
              />
            </svg>
            
            <!-- Info Icon -->
            <svg
              *ngIf="toast.type === 'info'"
              class="h-5 w-5 text-blue-400"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path
                fill-rule="evenodd"
                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                clip-rule="evenodd"
              />
            </svg>
          </div>
          
          <div class="ml-3 w-0 flex-1">
            <p [class]="getTextClasses(toast.type)" class="text-sm font-medium">
              {{ toast.message }}
            </p>
          </div>
          
          <div class="ml-4 flex-shrink-0 flex">
            <button
              (click)="removeToast(toast.id)"
              [class]="getCloseButtonClasses(toast.type)"
              class="rounded-md inline-flex focus:outline-none focus:ring-2 focus:ring-offset-2"
            >
              <span class="sr-only">Close</span>
              <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fill-rule="evenodd"
                  d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                  clip-rule="evenodd"
                />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    @keyframes slide-in {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }
    
    @keyframes fade-out {
      from {
        transform: translateX(0);
        opacity: 1;
      }
      to {
        transform: translateX(100%);
        opacity: 0;
      }
    }
    
    .animate-slide-in {
      animation: slide-in 0.3s ease-out;
    }
    
    .animate-fade-out {
      animation: fade-out 0.6s ease-in;
    }
    
    .toast-container {
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .toast-container:hover {
      transform: scale(1.02);
    }
  `]
})
export class ToastComponent implements OnInit, OnDestroy {
  private toastService = inject(ToastService);

  toasts: Toast[] = [];
  private subscription?: Subscription;

  ngOnInit(): void {
    this.subscription = this.toastService.toasts$.subscribe(
      toasts => this.toasts = toasts
    );
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  removeToast(id: string): void {
    this.toastService.removeToast(id);
  }

  trackByToastId(index: number, toast: Toast): string {
    return toast.id;
  }

  getToastClasses(type: string): string {
    const baseClasses = 'border-l-4';
    
    switch (type) {
      case 'success':
        return `${baseClasses} bg-green-50 border-green-400`;
      case 'error':
        return `${baseClasses} bg-red-50 border-red-400`;
      case 'warning':
        return `${baseClasses} bg-yellow-50 border-yellow-400`;
      case 'info':
        return `${baseClasses} bg-blue-50 border-blue-400`;
      default:
        return `${baseClasses} bg-gray-50 border-gray-400`;
    }
  }

  getTextClasses(type: string): string {
    switch (type) {
      case 'success':
        return 'text-green-800';
      case 'error':
        return 'text-red-800';
      case 'warning':
        return 'text-yellow-800';
      case 'info':
        return 'text-blue-800';
      default:
        return 'text-gray-800';
    }
  }

  getCloseButtonClasses(type: string): string {
    switch (type) {
      case 'success':
        return 'text-green-400 hover:text-green-600 focus:ring-green-500';
      case 'error':
        return 'text-red-400 hover:text-red-600 focus:ring-red-500';
      case 'warning':
        return 'text-yellow-400 hover:text-yellow-600 focus:ring-yellow-500';
      case 'info':
        return 'text-blue-400 hover:text-blue-600 focus:ring-blue-500';
      default:
        return 'text-gray-400 hover:text-gray-600 focus:ring-gray-500';
    }
  }
}
