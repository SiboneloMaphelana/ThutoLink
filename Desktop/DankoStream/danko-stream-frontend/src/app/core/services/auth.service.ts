import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { AuthRequest, AuthResponse } from '../models/auth.model';
import { WatchlistService } from './watchlist.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly baseUrl = '/api/auth';
  private readonly authStateSubject = new BehaviorSubject<AuthResponse | null>(null);

  readonly authState$ = this.authStateSubject.asObservable();

  constructor(
    private readonly http: HttpClient,
    private readonly watchlistService: WatchlistService
  ) {}

  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, payload).pipe(
      tap((response) => this.authStateSubject.next(response)),
      switchMap((response) =>
        this.watchlistService.loadWatchlist().pipe(
          map(() => response)
        )
      )
    );
  }

  register(payload: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, payload).pipe(
      tap((response) => this.authStateSubject.next(response)),
      switchMap((response) =>
        this.watchlistService.loadWatchlist().pipe(
          map(() => response)
        )
      )
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {}).pipe(
      tap(() => this.authStateSubject.next(null)),
      tap(() => this.watchlistService.reset())
    );
  }

  restoreSession(): Observable<boolean> {
    return this.http.get<AuthResponse>(`${this.baseUrl}/me`).pipe(
      tap((response) => this.authStateSubject.next(response)),
      switchMap(() =>
        this.watchlistService.loadWatchlist().pipe(
          map(() => true)
        )
      ),
      catchError(() => {
        this.authStateSubject.next(null);
        this.watchlistService.reset();
        return of(false);
      })
    );
  }

  clearSession(): void {
    this.authStateSubject.next(null);
    this.watchlistService.reset();
  }

  isAuthenticated(): boolean {
    return !!this.authStateSubject.value?.authenticated;
  }
}
