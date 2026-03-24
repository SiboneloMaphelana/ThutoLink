package com.movie.danko_stream_backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movie.danko_stream_backend.model.MovieDto;
import com.movie.danko_stream_backend.service.MoviesDatabaseService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

	private final MoviesDatabaseService moviesDatabaseService;

	public MovieController(MoviesDatabaseService moviesDatabaseService) {
		this.moviesDatabaseService = moviesDatabaseService;
	}

	@GetMapping
	public List<MovieDto> getMovies(
		@RequestParam(required = false) String genre,
		@RequestParam(defaultValue = "24") int limit
	) {
		return moviesDatabaseService.getMovies(genre, limit);
	}

	@GetMapping("/{id}")
	public MovieDto getMovieById(@PathVariable String id) {
		return moviesDatabaseService.getMovieById(id);
	}
}
