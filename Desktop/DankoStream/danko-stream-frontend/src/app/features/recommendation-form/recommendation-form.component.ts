import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { IntensityPreference, MovieMood, RuntimePreference } from '../../core/models/movie.model';

@Component({
  selector: 'app-recommendation-form',
  templateUrl: './recommendation-form.component.html',
  styleUrls: ['./recommendation-form.component.css']
})
export class RecommendationFormComponent {
  readonly moods: MovieMood[] = ['uplifting', 'tense', 'romantic', 'thoughtful', 'escapist', 'dark'];
  readonly runtimes: RuntimePreference[] = ['short', 'standard', 'long'];
  readonly intensities: IntensityPreference[] = ['gentle', 'balanced', 'intense'];
  readonly genres = ['Action', 'Adventure', 'Animation', 'Comedy', 'Crime', 'Drama', 'Family', 'Romance', 'Sci-Fi', 'Thriller'];

  model = {
    mood: 'uplifting' as MovieMood,
    runtime: 'standard' as RuntimePreference,
    intensity: 'balanced' as IntensityPreference,
    genres: ['Comedy', 'Adventure']
  };

  constructor(private readonly router: Router) {}

  submit(): void {
    this.router.navigate(['/results'], {
      queryParams: {
        mood: this.model.mood,
        runtime: this.model.runtime,
        intensity: this.model.intensity,
        genres: this.model.genres.join(',')
      }
    });
  }

  toggleGenre(genre: string): void {
    this.model.genres = this.model.genres.includes(genre)
      ? this.model.genres.filter((item) => item !== genre)
      : [...this.model.genres, genre];
  }
}
