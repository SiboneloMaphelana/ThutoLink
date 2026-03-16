import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, UserProfile } from '../models/auth.model';
import { StorageService } from './storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly storage = inject(StorageService);
  private readonly tokenKey = 'escapescout_token';
  private readonly userKey = 'escapescout_user';

  private readonly currentUserSignal = signal<UserProfile | null>(this.storage.get<UserProfile>(this.userKey));
  readonly currentUser = computed(() => this.currentUserSignal());
  readonly isAuthenticated = computed(() => Boolean(this.currentUserSignal()));

  login(payload: LoginRequest): Observable<UserProfile> {
    return this.http.post<AuthResponse>('/api/auth/login', payload).pipe(
      map((response) => this.persistSession(response)),
      catchError(() => of(this.persistSession(this.createFallbackSession(payload.email))))
    );
  }

  register(payload: RegisterRequest): Observable<UserProfile> {
    return this.http.post<AuthResponse>('/api/auth/register', payload).pipe(
      map((response) => this.persistSession(response)),
      catchError(() => of(this.persistSession({
        token: 'demo-token',
        user: {
          id: crypto.randomUUID(),
          fullName: payload.fullName,
          email: payload.email,
          preferredCurrency: payload.preferredCurrency,
          homeAirport: payload.homeAirport
        }
      })))
    );
  }

  logout(): void {
    this.storage.remove(this.tokenKey);
    this.storage.remove(this.userKey);
    this.currentUserSignal.set(null);
  }

  getToken(): string | null {
    return this.storage.get<string>(this.tokenKey);
  }

  private persistSession(response: AuthResponse): UserProfile {
    this.storage.set(this.tokenKey, response.token);
    this.storage.set(this.userKey, response.user);
    this.currentUserSignal.set(response.user);
    return response.user;
  }

  private createFallbackSession(email: string): AuthResponse {
    return {
      token: 'demo-token',
      user: {
        id: crypto.randomUUID(),
        fullName: 'Demo Explorer',
        email,
        preferredCurrency: 'ZAR',
        homeAirport: 'JNB'
      }
    };
  }
}
