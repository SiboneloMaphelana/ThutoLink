import { Injectable } from '@angular/core';
import { LoginResponse, UserSummary } from '../models/platform.models';

const TOKEN_KEY = 'thutolink.token';
const USER_KEY = 'thutolink.user';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  user(): UserSummary | null {
    const stored = localStorage.getItem(USER_KEY);
    if (!stored) {
      return null;
    }

    try {
      return JSON.parse(stored) as UserSummary;
    } catch {
      localStorage.removeItem(USER_KEY);
      return null;
    }
  }

  setSession(session: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, session.token);
    localStorage.setItem(USER_KEY, JSON.stringify(session.user));
  }

  clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
}
