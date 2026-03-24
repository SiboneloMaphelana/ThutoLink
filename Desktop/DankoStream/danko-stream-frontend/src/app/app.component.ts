import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthResponse } from './core/models/auth.model';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  readonly navigation = [
    { label: 'Home', path: '/' },
    { label: 'Recommend', path: '/recommend' },
    { label: 'Results', path: '/results' },
    { label: 'Compare', path: '/compare' },
    { label: 'Watchlist', path: '/watchlist' }
  ];

  authState: AuthResponse | null = null;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.authService.authState$.subscribe((state) => {
      this.authState = state;
    });

    this.authService.restoreSession().subscribe();
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: () => {
        this.authService.clearSession();
        this.router.navigate(['/login']);
      }
    });
  }
}
