export type MovieMood = 'uplifting' | 'tense' | 'romantic' | 'thoughtful' | 'escapist' | 'dark';
export type RuntimePreference = 'short' | 'standard' | 'long';
export type IntensityPreference = 'gentle' | 'balanced' | 'intense';

export interface Movie {
  id: string;
  title: string;
  year: number;
  runtime: number;
  intensity: IntensityPreference;
  moods: MovieMood[];
  genres: string[];
  overview: string;
  posterUrl: string;
  backdropUrl: string;
  rating: number;
  voteCount: number;
  language: string;
  director: string;
  cast: string[];
  watchProviders: string[];
}

export interface RecommendationQuery {
  mood: MovieMood;
  runtime: RuntimePreference;
  intensity: IntensityPreference;
  genres: string[];
}

export interface RecommendationResult {
  movie: Movie;
  score: number;
  breakdown: {
    mood: number;
    runtime: number;
    intensity: number;
    genre: number;
    quality: number;
  };
  rationale: string[];
}

export interface CompareResult {
  left: Movie;
  right: Movie;
  winnerId: string;
  verdict: string;
}
