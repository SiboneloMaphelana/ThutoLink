package com.movie.danko_stream_backend.model;

import java.util.List;

public record MovieDto(
	String id,
	String title,
	int year,
	int runtime,
	String intensity,
	List<String> moods,
	List<String> genres,
	String overview,
	String posterUrl,
	String backdropUrl,
	double rating,
	int voteCount,
	String language,
	String director,
	List<String> cast,
	List<String> watchProviders
) {
}
