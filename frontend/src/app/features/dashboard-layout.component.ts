import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../core/auth/auth.service';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './dashboard-layout.component.html'
})
export class DashboardLayoutComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthService);

  currentUser: any = null;

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  isCurrentRoute(route: string): boolean {
    return this.router.url.startsWith(route);
  }

  logout(): void {
    this.authService.logout();
  }
}