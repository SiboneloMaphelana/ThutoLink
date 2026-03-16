import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from 'src/app/core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app-shell.component.html',
  styleUrls: ['./app-shell.component.css']
})
export class AppShellComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly isAuthenticated = this.authService.isAuthenticated;
  readonly currentUserName = computed(() => this.authService.currentUser()?.fullName ?? 'Guest');

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['/search']);
  }
}
