package com.movie.danko_stream_backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
	@NotBlank @Size(min = 1, max = 50) String username,
	@NotBlank @Size(min = 1, max = 100) String password
) {
}
