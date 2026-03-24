import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Movie, RecommendationResult } from '../../../core/models/movie.model';

@Component({
  selector: 'app-movie-card',
  templateUrl: './movie-card.component.html',
  styleUrls: ['./movie-card.component.css']
})
export class MovieCardComponent {
  @Input() movie?: Movie;
  @Input() recommendation?: RecommendationResult;
  @Input() saved = false;
  @Input() mode: 'default' | 'compact' = 'default';

  @Output() saveToggle = new EventEmitter<string>();

  onToggleSave(): void {
    if (this.movie) {
      this.saveToggle.emit(this.movie.id);
    }
  }
}
