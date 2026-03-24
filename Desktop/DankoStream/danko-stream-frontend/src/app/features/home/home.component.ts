import { Component } from '@angular/core';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  readonly capabilities = [
    'Mood, runtime, and intensity based recommendation form',
    'Results ranked by a weighted custom score',
    'Movie details, comparison, and persistent watchlist',
    'RapidAPI-ready backend contract with caching and filtering'
  ];

  readonly endpoints = [
    'GET /api/movies?genre=Action&runtimeMax=120',
    'POST /api/recommendations',
    'GET /api/movies/:id',
    'GET /api/compare?left=dune-part-two&right=arrival',
    'GET /api/watchlist',
    'POST /api/watchlist'
  ];
}
