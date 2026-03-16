import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, delay, of, tap } from 'rxjs';
import { DestinationDetail, DestinationRecommendation, SortOption, TripSearchRequest } from '../models/trip.model';
import { StorageService } from './storage.service';

@Injectable({ providedIn: 'root' })
export class TripSearchService {
  private readonly http = inject(HttpClient);
  private readonly storage = inject(StorageService);

  private readonly searchRequestKey = 'escapescout_last_search';
  private readonly resultsSignal = signal<DestinationRecommendation[]>([]);
  private readonly lastRequestSignal = signal<TripSearchRequest | null>(this.storage.get<TripSearchRequest>(this.searchRequestKey));

  readonly results = this.resultsSignal.asReadonly();
  readonly lastRequest = this.lastRequestSignal.asReadonly();

  searchTrips(request: TripSearchRequest): Observable<DestinationRecommendation[]> {
    this.storage.set(this.searchRequestKey, request);
    this.lastRequestSignal.set(request);

    return this.http.post<DestinationRecommendation[]>('/api/trips/search', request).pipe(
      tap((results) => this.resultsSignal.set(results)),
      catchError(() => of(this.buildMockResults(request)).pipe(
        delay(400),
        tap((results) => this.resultsSignal.set(results))
      ))
    );
  }

  getDestinationDetail(destinationId: string): Observable<DestinationDetail> {
    return this.http.get<DestinationDetail>(`/api/trips/${destinationId}`).pipe(
      catchError(() => of(this.buildMockDetail(destinationId)).pipe(delay(250)))
    );
  }

  sortResults(results: DestinationRecommendation[], sortBy: SortOption): DestinationRecommendation[] {
    const copy = [...results];
    switch (sortBy) {
      case 'CHEAPEST':
        return copy.sort((a, b) => a.totalEstimatedCost - b.totalEstimatedCost);
      case 'BEST_WEATHER':
        return copy.sort((a, b) => b.weatherScore - a.weatherScore);
      default:
        return copy.sort((a, b) => b.score - a.score);
    }
  }

  private buildMockResults(request: TripSearchRequest): DestinationRecommendation[] {
    const travelerMultiplier = Math.max(request.travelers, 1);
    return [
      {
        id: 'cpt-weekend',
        destinationCity: 'Cape Town',
        country: 'South Africa',
        region: request.region,
        score: 92,
        flightPrice: 2400 * travelerMultiplier,
        hotelPrice: 3100,
        totalEstimatedCost: 5500 + (travelerMultiplier - 1) * 2400,
        weatherSummary: 'Sunny, 24°C',
        weatherScore: 90,
        imageUrl: 'https://images.unsplash.com/photo-1580060839134-75a5edca2e99?auto=format&fit=crop&w=1200&q=80',
        topAttractions: [
          { name: 'Table Mountain', category: 'Landmark', rating: 4.9, address: 'Cape Town' },
          { name: 'V&A Waterfront', category: 'Waterfront', rating: 4.8, address: 'Granger Bay' },
          { name: 'Bo-Kaap', category: 'Culture', rating: 4.7, address: 'Cape Town CBD' }
        ],
        flight: {
          airline: 'FlySafair',
          departureTime: '07:15',
          returnTime: '19:40',
          duration: '2h 10m',
          price: 2400 * travelerMultiplier
        },
        hotel: {
          name: 'Harbour Edge Hotel',
          area: 'Green Point',
          rating: 4.6,
          pricePerNight: 1550,
          totalPrice: 3100
        }
      },
      {
        id: 'dur-weekend',
        destinationCity: 'Durban',
        country: 'South Africa',
        region: request.region,
        score: 86,
        flightPrice: 1950 * travelerMultiplier,
        hotelPrice: 2600,
        totalEstimatedCost: 4550 + (travelerMultiplier - 1) * 1950,
        weatherSummary: 'Warm, light breeze, 27°C',
        weatherScore: 88,
        imageUrl: 'https://images.unsplash.com/photo-1544989164-31d2bf36b9ab?auto=format&fit=crop&w=1200&q=80',
        topAttractions: [
          { name: 'uShaka Marine World', category: 'Family', rating: 4.6, address: 'Durban Point' },
          { name: 'Golden Mile', category: 'Beach', rating: 4.5, address: 'Durban Beachfront' },
          { name: 'Moses Mabhida Stadium', category: 'Landmark', rating: 4.6, address: 'Stamford Hill' }
        ],
        flight: {
          airline: 'Lift',
          departureTime: '08:00',
          returnTime: '20:15',
          duration: '1h 05m',
          price: 1950 * travelerMultiplier
        },
        hotel: {
          name: 'Oceanview Suites',
          area: 'Umhlanga',
          rating: 4.4,
          pricePerNight: 1300,
          totalPrice: 2600
        }
      },
      {
        id: 'wdh-weekend',
        destinationCity: 'Windhoek',
        country: 'Namibia',
        region: request.region,
        score: 81,
        flightPrice: 3150 * travelerMultiplier,
        hotelPrice: 2250,
        totalEstimatedCost: 5400 + (travelerMultiplier - 1) * 3150,
        weatherSummary: 'Dry and clear, 29°C',
        weatherScore: 84,
        imageUrl: 'https://images.unsplash.com/photo-1516026672322-bc52d61a55d5?auto=format&fit=crop&w=1200&q=80',
        topAttractions: [
          { name: 'Christuskirche', category: 'Culture', rating: 4.5, address: 'Windhoek Central' },
          { name: 'Namibia Craft Centre', category: 'Shopping', rating: 4.7, address: 'Tal Street' },
          { name: 'Daan Viljoen Reserve', category: 'Adventure', rating: 4.6, address: 'Khomas Highlands' }
        ],
        flight: {
          airline: 'Airlink',
          departureTime: '06:45',
          returnTime: '18:30',
          duration: '2h 15m',
          price: 3150 * travelerMultiplier
        },
        hotel: {
          name: 'Savanna Stay',
          area: 'City Centre',
          rating: 4.3,
          pricePerNight: 1125,
          totalPrice: 2250
        }
      }
    ];
  }

  private buildMockDetail(destinationId: string): DestinationDetail {
    const destination = this.resultsSignal().find((item) => item.id === destinationId) ?? this.buildMockResults({
      origin: 'JNB',
      startDate: '2026-03-20',
      endDate: '2026-03-22',
      budget: 8000,
      travelers: 2,
      tripStyle: 'CULTURE',
      region: 'AFRICA'
    })[0];

    return {
      ...destination,
      overview: `${destination.destinationCity} is a strong weekend option with manageable travel time, a balanced spend profile, and attractions aligned to short-stay leisure travel.`,
      dailyForecast: [
        { day: 'Friday', condition: 'Sunny', minTemp: 18, maxTemp: 24, rainChance: 5 },
        { day: 'Saturday', condition: 'Clear', minTemp: 17, maxTemp: 25, rainChance: 8 },
        { day: 'Sunday', condition: 'Partly cloudy', minTemp: 18, maxTemp: 23, rainChance: 15 }
      ],
      itinerary: [
        'Friday: arrival, hotel check-in, sunset viewpoint, casual dinner nearby.',
        'Saturday: flagship attraction in the morning, lunch in a popular district, second attraction in the afternoon.',
        'Sunday: relaxed brunch, souvenir stop, airport transfer.'
      ],
      tags: ['Weekend-ready', 'Good weather', 'Short flight']
    };
  }
}
