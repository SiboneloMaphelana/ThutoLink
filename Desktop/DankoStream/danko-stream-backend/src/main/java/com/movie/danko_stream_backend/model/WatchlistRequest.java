package com.movie.danko_stream_backend.model;

import jakarta.validation.constraints.NotBlank;

public record WatchlistRequest(
	@NotBlank String movieId
) {
}
