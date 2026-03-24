import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Movie } from '../../core/models/movie.model';
import { MovieApiService } from '../../core/services/movie-api.service';
import { RecommendationService } from '../../core/services/recommendation.service';

@Component({
  selector: 'app-compare',
  templateUrl: './compare.component.html',
  styleUrls: ['./compare.component.css']
})
export class CompareComponent implements OnInit {
  movies: Movie[] = [];
  selectedLeft = '';
  selectedRight = '';
  verdict = '';
  winnerId = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly movieApiService: MovieApiService,
    private readonly recommendationService: RecommendationService
  ) {}

  ngOnInit(): void {
    this.movieApiService.getMovies().subscribe((movies) => {
      this.movies = movies;

      const fallbackLeft = movies[0]?.id ?? '';
      const fallbackRight = movies[1]?.id ?? fallbackLeft;
      const left = this.route.snapshot.queryParamMap.get('left');
      const right = this.route.snapshot.queryParamMap.get('right');

      this.selectedLeft = left ?? fallbackLeft;
      this.selectedRight = right ?? fallbackRight;

      if (this.selectedLeft && this.selectedRight) {
        this.runCompare();
      }
    });
  }

  runCompare(): void {
    this.recommendationService.compareMovies(this.selectedLeft, this.selectedRight).subscribe((result) => {
      this.verdict = result?.verdict ?? 'Pick two valid movies to compare.';
      this.winnerId = result?.winnerId ?? '';
    });
  }

  getMovie(id: string): Movie | undefined {
    return this.movies.find((movie) => movie.id === id);
  }
}
