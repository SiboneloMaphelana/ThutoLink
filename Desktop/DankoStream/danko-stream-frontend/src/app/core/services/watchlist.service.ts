import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class WatchlistService {
  private readonly baseUrl = '/api/watchlist';
  private readonly watchlistSubject = new BehaviorSubject<string[]>([]);

  constructor(private readonly http: HttpClient) {}

  get watchlist$(): Observable<string[]> {
    return this.watchlistSubject.asObservable();
  }

  get ids(): string[] {
    return this.watchlistSubject.value;
  }

  loadWatchlist(): Observable<string[]> {
    return this.http.get<string[]>(this.baseUrl).pipe(
      tap((ids) => this.watchlistSubject.next(ids))
    );
  }

  toggle(movieId: string): Observable<string[]> {
    const currentIds = this.watchlistSubject.value;
    return currentIds.includes(movieId) ? this.remove(movieId) : this.add(movieId);
  }

  add(movieId: string): Observable<string[]> {
    return this.http.post<string[]>(this.baseUrl, { movieId }).pipe(
      tap((ids) => this.watchlistSubject.next(ids))
    );
  }

  remove(movieId: string): Observable<string[]> {
    return this.http.delete<string[]>(`${this.baseUrl}/${movieId}`).pipe(
      tap((ids) => this.watchlistSubject.next(ids))
    );
  }

  isSaved(movieId: string): boolean {
    return this.watchlistSubject.value.includes(movieId);
  }

  reset(): void {
    this.watchlistSubject.next([]);
  }

  restoreOrReset(): Observable<string[]> {
    return this.loadWatchlist().pipe(
      catchError(() => {
        this.reset();
        return of([]);
      })
    );
  }
}
