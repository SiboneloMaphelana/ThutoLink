import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import { AppIconComponent } from '../../shared/app-icon.component';
import { DemoCredential } from '../../core/models/platform.models';
import { AuthActions } from '../../core/store/auth/auth.actions';
import { selectAuthError, selectAuthLoading, selectDemoCredentials } from '../../core/store/auth/auth.selectors';

@Component({
  selector: 'app-auth-page',
  standalone: true,
  imports: [CommonModule, FormsModule, AppIconComponent],
  templateUrl: './auth-page.component.html'
})
export class AuthPageComponent {
  private readonly store = inject(Store);

  readonly email = signal('teacher.nkosi@thutolink.school');
  readonly password = signal('teacher123');
  readonly loading = this.store.selectSignal(selectAuthLoading);
  readonly error = this.store.selectSignal(selectAuthError);
  readonly demoCredentials = this.store.selectSignal(selectDemoCredentials);

  submit(): void {
    this.store.dispatch(AuthActions.loginRequested({ email: this.email().trim(), password: this.password().trim() }));
  }

  applyDemoCredential(credential: DemoCredential): void {
    this.email.set(credential.email);
    this.password.set(credential.password);
  }
}
