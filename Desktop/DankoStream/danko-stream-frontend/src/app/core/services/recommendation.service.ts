import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  CompareResult,
  IntensityPreference,
  Movie,
  RecommendationQuery,
  RecommendationResult,
  RuntimePreference
} from '../models/movie.model';
import { MovieApiService } from './movie-api.service';

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  constructor(private readonly movieApiService: MovieApiService) {}

  getRecommendations(query: RecommendationQuery): Observable<RecommendationResult[]> {
    return this.movieApiService.getMovies().pipe(
      map((movies) =>
        movies
          .map((movie) => this.scoreMovie(movie, query))
          .filter((item) => item.score >= 45)
          .sort((left, right) => right.score - left.score)
          .slice(0, 6)
      )
    );
  }

  compareMovies(leftId: string, rightId: string): Observable<CompareResult | undefined> {
    return this.movieApiService.getMovies().pipe(
      map((movies) => {
        const left = movies.find((movie) => movie.id === leftId);
        const right = movies.find((movie) => movie.id === rightId);

        if (!left || !right) {
          return undefined;
        }

        const leftComposite = this.baseCompositeScore(left);
        const rightComposite = this.baseCompositeScore(right);
        const winnerId = leftComposite >= rightComposite ? left.id : right.id;

        return {
          left,
          right,
          winnerId,
          verdict:
            winnerId === left.id
              ? `${left.title} edges ahead on quality, momentum, and overall flexibility.`
              : `${right.title} edges ahead on quality, momentum, and overall flexibility.`
        };
      })
    );
  }

  private scoreMovie(movie: Movie, query: RecommendationQuery): RecommendationResult {
    const breakdown = {
      mood: movie.moods.includes(query.mood) ? 30 : 8,
      runtime: this.runtimeScore(movie.runtime, query.runtime),
      intensity: movie.intensity === query.intensity ? 20 : this.intensityNearMatch(movie.intensity, query.intensity),
      genre: this.genreScore(movie.genres, query.genres),
      quality: Math.round((movie.rating / 10) * 20)
    };

    const score = breakdown.mood + breakdown.runtime + breakdown.intensity + breakdown.genre + breakdown.quality;
    const rationale = [
      `${movie.title} fits a ${query.mood} mood with ${movie.moods.join(', ')} energy.`,
      `${movie.runtime} minutes lands well for a ${query.runtime} runtime window.`,
      `Rated ${movie.rating}/10 with ${movie.voteCount.toLocaleString()} votes.`
    ];

    return { movie, score, breakdown, rationale };
  }

  private runtimeScore(runtime: number, preference: RuntimePreference): number {
    if (preference === 'short') {
      return runtime <= 100 ? 15 : runtime <= 125 ? 8 : 2;
    }

    if (preference === 'standard') {
      return runtime >= 95 && runtime <= 130 ? 15 : runtime <= 145 ? 9 : 3;
    }

    return runtime >= 125 ? 15 : runtime >= 110 ? 9 : 4;
  }

  private intensityNearMatch(movieIntensity: IntensityPreference, desiredIntensity: IntensityPreference): number {
    const levels: IntensityPreference[] = ['gentle', 'balanced', 'intense'];
    const distance = Math.abs(levels.indexOf(movieIntensity) - levels.indexOf(desiredIntensity));
    return distance === 1 ? 12 : 4;
  }

  private genreScore(movieGenres: string[], desiredGenres: string[]): number {
    if (!desiredGenres.length) {
      return 10;
    }

    const matches = desiredGenres.filter((genre) => movieGenres.includes(genre)).length;
    return Math.min(matches * 8, 15);
  }

  private baseCompositeScore(movie: Movie): number {
    const ratingScore = movie.rating * 10;
    const popularityScore = Math.min(movie.voteCount / 100, 25);
    const versatilityScore = movie.moods.length * 4 + movie.genres.length * 2;
    return ratingScore + popularityScore + versatilityScore;
  }
}
