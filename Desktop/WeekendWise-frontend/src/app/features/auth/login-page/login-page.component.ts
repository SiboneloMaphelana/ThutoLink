import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.component.html',
  styleUrls: ['../auth-page.component.css']
})
export class LoginPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  isSubmitting = false;

  readonly form = this.fb.nonNullable.group({
    email: ['demo@escapescout.app', [Validators.required, Validators.email]],
    password: ['password123', [Validators.required, Validators.minLength(8)]]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.authService.login(this.form.getRawValue()).pipe(
      finalize(() => this.isSubmitting = false)
    ).subscribe(() => void this.router.navigate(['/search']));
  }
}
