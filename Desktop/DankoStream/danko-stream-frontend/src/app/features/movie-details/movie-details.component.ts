import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Movie } from '../../core/models/movie.model';
import { MovieApiService } from '../../core/services/movie-api.service';
import { WatchlistService } from '../../core/services/watchlist.service';

@Component({
  selector: 'app-movie-details',
  templateUrl: './movie-details.component.html',
  styleUrls: ['./movie-details.component.css']
})
export class MovieDetailsComponent implements OnInit {
  movie?: Movie;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly movieApiService: MovieApiService,
    private readonly watchlistService: WatchlistService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (!id) {
        return;
      }

      this.movieApiService.getMovieById(id).subscribe((movie) => {
        this.movie = movie;
      });
    });
  }

  toggleSave(): void {
    if (this.movie) {
      this.watchlistService.toggle(this.movie.id).subscribe();
    }
  }

  isSaved(): boolean {
    return !!this.movie && this.watchlistService.isSaved(this.movie.id);
  }
}
