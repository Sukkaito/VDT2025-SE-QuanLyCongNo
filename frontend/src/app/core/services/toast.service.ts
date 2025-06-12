import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration?: number;
  timestamp: number;
  isRemoving?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  public toasts$ = this.toastsSubject.asObservable();

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9);
  }

  private addToast(type: Toast['type'], message: string, duration: number = 6000): void {
    const toast: Toast = {
      id: this.generateId(),
      type,
      message,
      duration,
      timestamp: Date.now(),
      isRemoving: false
    };

    const currentToasts = this.toastsSubject.value;
    this.toastsSubject.next([...currentToasts, toast]);

    // Auto remove toast after duration
    if (duration > 0) {
      setTimeout(() => {
        this.removeToast(toast.id);
      }, duration);
    }
  }

  success(message: string, duration?: number): void {
    this.addToast('success', message, duration);
  }

  error(message: string, duration?: number): void {
    this.addToast('error', message, duration);
  }

  warning(message: string, duration?: number): void {
    this.addToast('warning', message, duration);
  }

  info(message: string, duration?: number): void {
    this.addToast('info', message, duration);
  }

  removeToast(id: string): void {
    const currentToasts = this.toastsSubject.value;
    const toastIndex = currentToasts.findIndex(toast => toast.id === id);
    
    if (toastIndex > -1) {
      // Mark for removal animation
      const updatedToasts = [...currentToasts];
      updatedToasts[toastIndex] = { ...updatedToasts[toastIndex], isRemoving: true };
      this.toastsSubject.next(updatedToasts);
      
      // Actually remove after animation
      setTimeout(() => {
        const finalToasts = this.toastsSubject.value.filter(toast => toast.id !== id);
        this.toastsSubject.next(finalToasts);
      }, 600); // Match fade-out animation duration
    }
  }

  clearAll(): void {
    this.toastsSubject.next([]);
  }
}
