import { Injectable, inject, signal } from '@angular/core';
import { DestinationRecommendation } from '../models/trip.model';
import { StorageService } from './storage.service';

@Injectable({ providedIn: 'root' })
export class FavoritesService {
  private readonly storage = inject(StorageService);
  private readonly storageKey = 'escapescout_favorites';
  private readonly favoritesSignal = signal<DestinationRecommendation[]>(
    this.storage.get<DestinationRecommendation[]>(this.storageKey) ?? []
  );
  readonly favorites = this.favoritesSignal.asReadonly();

  saveTrip(trip: DestinationRecommendation): void {
    const current = this.favoritesSignal();
    if (current.some((item) => item.id === trip.id)) {
      return;
    }

    const updated = [...current, trip];
    this.favoritesSignal.set(updated);
    this.storage.set(this.storageKey, updated);
  }

  removeTrip(tripId: string): void {
    const updated = this.favoritesSignal().filter((item) => item.id !== tripId);
    this.favoritesSignal.set(updated);
    this.storage.set(this.storageKey, updated);
  }

  isFavorite(tripId: string): boolean {
    return this.favoritesSignal().some((item) => item.id === tripId);
  }
}
