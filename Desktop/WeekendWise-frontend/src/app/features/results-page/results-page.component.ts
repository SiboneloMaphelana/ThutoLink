import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SortOption, DestinationRecommendation } from 'src/app/core/models/trip.model';
import { FavoritesService } from 'src/app/core/services/favorite.service';
import { TripSearchService } from 'src/app/core/services/trip-search.service';


@Component({
  selector: 'app-results-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './results-page.component.html',
  styleUrls: ['./results-page.component.css']
})
export class ResultsPageComponent {
  private readonly tripSearchService = inject(TripSearchService);
  private readonly favoritesService = inject(FavoritesService);

  readonly sortBy = signal<SortOption>('BEST_MATCH');
  readonly results = computed(() => this.tripSearchService.sortResults(this.tripSearchService.results(), this.sortBy()));
  readonly lastRequest = this.tripSearchService.lastRequest;

  setSort(sortBy: SortOption): void {
    this.sortBy.set(sortBy);
  }

  toggleFavorite(result: DestinationRecommendation): void {
    if (this.favoritesService.isFavorite(result.id)) {
      this.favoritesService.removeTrip(result.id);
      return;
    }

    this.favoritesService.saveTrip(result);
  }

  isFavorite(resultId: string): boolean {
    return this.favoritesService.isFavorite(resultId);
  }
}
