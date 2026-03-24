import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  model = {
    username: '',
    password: ''
  };

  loading = false;
  error = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    readonly route: ActivatedRoute
  ) {}

  submit(): void {
    this.loading = true;
    this.error = '';

    this.authService.login(this.model).subscribe({
      next: () => {
        const redirectTo = this.route.snapshot.queryParamMap.get('redirectTo') || '/recommend';
        this.loading = false;
        this.router.navigateByUrl(redirectTo);
      },
      error: () => {
        this.loading = false;
        this.error = 'Invalid username or password.';
      }
    });
  }
}
