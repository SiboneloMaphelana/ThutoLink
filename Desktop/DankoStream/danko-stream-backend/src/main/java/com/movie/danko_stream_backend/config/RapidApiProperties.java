package com.movie.danko_stream_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rapidapi.moviesdatabase")
public record RapidApiProperties(
	String baseUrl,
	String host,
	String key
) {
}
