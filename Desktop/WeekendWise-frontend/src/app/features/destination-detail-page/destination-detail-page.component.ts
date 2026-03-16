import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DestinationDetail } from 'src/app/core/models/trip.model';
import { FavoritesService } from 'src/app/core/services/favorite.service';
import { TripSearchService } from 'src/app/core/services/trip-search.service';


@Component({
  selector: 'app-destination-detail-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './destination-detail-page.component.html',
  styleUrls: ['./destination-detail-page.component.css']
})
export class DestinationDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly tripSearchService = inject(TripSearchService);
  private readonly favoritesService = inject(FavoritesService);

  readonly detail = signal<DestinationDetail | null>(null);
  readonly isLoading = signal(true);

  ngOnInit(): void {
    const destinationId = this.route.snapshot.paramMap.get('id');
    if (!destinationId) {
      this.isLoading.set(false);
      return;
    }

    this.tripSearchService.getDestinationDetail(destinationId).subscribe((detail) => {
      this.detail.set(detail);
      this.isLoading.set(false);
    });
  }

  toggleFavorite(): void {
    const detail = this.detail();
    if (!detail) {
      return;
    }

    if (this.favoritesService.isFavorite(detail.id)) {
      this.favoritesService.removeTrip(detail.id);
      return;
    }

    this.favoritesService.saveTrip(detail);
  }

  isFavorite(destinationId: string): boolean {
    return this.favoritesService.isFavorite(destinationId);
  }
}
