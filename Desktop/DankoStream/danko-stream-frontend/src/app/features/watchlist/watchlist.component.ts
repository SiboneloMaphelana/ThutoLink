import { Component, OnInit } from '@angular/core';
import { Movie } from '../../core/models/movie.model';
import { MovieApiService } from '../../core/services/movie-api.service';
import { WatchlistService } from '../../core/services/watchlist.service';

@Component({
  selector: 'app-watchlist',
  templateUrl: './watchlist.component.html',
  styleUrls: ['./watchlist.component.css']
})
export class WatchlistComponent implements OnInit {
  savedMovies: Movie[] = [];
  loading = true;

  constructor(
    private readonly movieApiService: MovieApiService,
    private readonly watchlistService: WatchlistService
  ) {}

  ngOnInit(): void {
    this.watchlistService.watchlist$.subscribe((ids) => {
      this.movieApiService.getMovies().subscribe((movies) => {
        this.savedMovies = movies.filter((movie) => ids.includes(movie.id));
        this.loading = false;
      });
    });

    this.watchlistService.loadWatchlist().subscribe({
      error: () => {
        this.loading = false;
      }
    });
  }

  toggleSave(movieId: string): void {
    this.watchlistService.toggle(movieId).subscribe();
  }

  isSaved(movieId: string): boolean {
    return this.watchlistService.isSaved(movieId);
  }
}
