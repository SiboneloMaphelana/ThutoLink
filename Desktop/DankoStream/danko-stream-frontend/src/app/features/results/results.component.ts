import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RecommendationQuery, RecommendationResult } from '../../core/models/movie.model';
import { RecommendationService } from '../../core/services/recommendation.service';
import { WatchlistService } from '../../core/services/watchlist.service';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css']
})
export class ResultsComponent implements OnInit {
  loading = true;
  query?: RecommendationQuery;
  recommendations: RecommendationResult[] = [];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly recommendationService: RecommendationService,
    private readonly watchlistService: WatchlistService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      this.loading = true;

      this.query = {
        mood: (params.get('mood') as RecommendationQuery['mood']) || 'uplifting',
        runtime: (params.get('runtime') as RecommendationQuery['runtime']) || 'standard',
        intensity: (params.get('intensity') as RecommendationQuery['intensity']) || 'balanced',
        genres: params.get('genres') ? params.get('genres')!.split(',').filter(Boolean) : []
      };

      this.recommendationService.getRecommendations(this.query).subscribe((results) => {
        this.recommendations = results;
        this.loading = false;
      });
    });
  }

  isSaved(movieId: string): boolean {
    return this.watchlistService.isSaved(movieId);
  }

  toggleSave(movieId: string): void {
    this.watchlistService.toggle(movieId).subscribe();
  }
}
