import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Movie } from '../models/movie.model';

@Injectable({
  providedIn: 'root'
})
export class MovieApiService {
  private readonly backendBaseUrl = '/api';

  constructor(private readonly http: HttpClient) {}

  getMovies(): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.backendBaseUrl}/movies`);
  }

  getMovieById(id: string): Observable<Movie | undefined> {
    return this.http.get<Movie>(`${this.backendBaseUrl}/movies/${id}`);
  }
}
