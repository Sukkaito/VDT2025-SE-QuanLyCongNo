import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);

  loginForm!: FormGroup;
  loading = false;
  submitted = false;
  errorMessage = '';

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/']);
    }
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  // convenience getter for easy access to form fields
  get f() { 
    return this.loginForm.controls; 
  }
  onSubmit(): void {
    this.submitted = true;
    this.errorMessage = '';

    // stop here if form is invalid
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    
    const { username, password } = this.loginForm.value;
    
    this.authService.login(username, password)
      .subscribe({
        next: () => {
          this.router.navigate(['/']);
        },
        error: (error: any) => {
          this.errorMessage = error?.error?.message || 'Login failed. Please check your credentials.';
          this.loading = false;
        }
      });
  }
}