import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FavoritesService } from 'src/app/core/services/favorite.service';

@Component({
  selector: 'app-favorites-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './favorites-page.component.html',
  styleUrls: ['./favorites-page.component.css']
})
export class FavoritesPageComponent {
  readonly favorites = inject(FavoritesService).favorites;
  private readonly favoritesService = inject(FavoritesService);

  remove(tripId: string): void {
    this.favoritesService.removeTrip(tripId);
  }
}
