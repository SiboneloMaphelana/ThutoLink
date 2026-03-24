import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['../login/login.component.css']
})
export class RegisterComponent {
  model = {
    username: '',
    password: '',
    confirmPassword: ''
  };

  loading = false;
  error = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    readonly route: ActivatedRoute
  ) {}

  submit(): void {
    this.error = '';

    if (this.model.password !== this.model.confirmPassword) {
      this.error = 'Passwords do not match.';
      return;
    }

    if (this.model.password.length < 8) {
      this.error = 'Password must be at least 8 characters.';
      return;
    }

    if (this.model.username.trim().length < 3) {
      this.error = 'Username must be at least 3 characters.';
      return;
    }

    this.loading = true;

    this.authService
      .register({
        username: this.model.username.trim(),
        password: this.model.password
      })
      .subscribe({
        next: () => {
          const redirectTo = this.route.snapshot.queryParamMap.get('redirectTo') || '/recommend';
          this.loading = false;
          this.router.navigateByUrl(redirectTo);
        },
        error: (err) => {
          this.loading = false;
          if (err.status === 409) {
            this.error = 'That username is already taken.';
          } else if (err.status === 400) {
            this.error = 'Please check username and password requirements.';
          } else {
            this.error = 'Registration failed. Please try again.';
          }
        }
      });
  }
}
