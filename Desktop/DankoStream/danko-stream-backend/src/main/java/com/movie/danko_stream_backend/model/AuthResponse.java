package com.movie.danko_stream_backend.model;

import java.util.List;

public record AuthResponse(
	boolean authenticated,
	String username,
	List<String> roles
) {
}
