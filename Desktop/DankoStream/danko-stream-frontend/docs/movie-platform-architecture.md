# DankoStream Movie Recommendation Platform

## Production-style structure

```text
src/
  app/
    core/
      data/mock-movies.ts
      models/movie.model.ts
      services/movie-api.service.ts
      services/recommendation.service.ts
      services/watchlist.service.ts
    features/
      home/
      recommendation-form/
      results/
      movie-details/
      compare/
      watchlist/
    shared/
      components/movie-card/
      components/section-header/
```

## Suggested backend structure

```text
backend/
  src/
    config/rapidapi.ts
    routes/movies.routes.ts
    routes/recommendations.routes.ts
    routes/watchlist.routes.ts
    controllers/movies.controller.ts
    controllers/recommendations.controller.ts
    services/rapidapi-movie.service.ts
    services/cache.service.ts
    services/recommendation-ranking.service.ts
    repositories/watchlist.repository.ts
    models/movie.dto.ts
    app.ts
```

## REST endpoints

- `GET /api/movies`
- `GET /api/movies/:id`
- `POST /api/recommendations`
- `GET /api/compare?left=:id&right=:id`
- `GET /api/watchlist`
- `POST /api/watchlist`
- `DELETE /api/watchlist/:id`

## RapidAPI integration flow

1. Backend calls a RapidAPI movie endpoint such as `moviesdatabase.p.rapidapi.com`.
2. Normalize fields into your internal `Movie` DTO.
3. Cache popular responses by genre, title lookup, and detail id.
4. Apply business filters for runtime, mood mapping, and intensity.
5. Rank server-side so clients stay thin and consistent.

## Example recommendation logic

```ts
score =
  moodMatch * 30 +
  runtimeFit * 15 +
  intensityFit * 20 +
  genreOverlap * 15 +
  normalizedRating * 20;
```

## Example backend contract

```json
POST /api/recommendations
{
  "mood": "uplifting",
  "runtime": "standard",
  "intensity": "balanced",
  "genres": ["Comedy", "Adventure"]
}
```

```json
200 OK
{
  "results": [
    {
      "movieId": "paddington-two",
      "score": 88,
      "breakdown": {
        "mood": 30,
        "runtime": 15,
        "intensity": 20,
        "genre": 8,
        "quality": 15
      }
    }
  ]
}
```
