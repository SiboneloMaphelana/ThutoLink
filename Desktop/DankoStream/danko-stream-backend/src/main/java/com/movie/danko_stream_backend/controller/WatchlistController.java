package com.movie.danko_stream_backend.controller;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.movie.danko_stream_backend.model.WatchlistRequest;
import com.movie.danko_stream_backend.service.WatchlistService;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

	private final WatchlistService watchlistService;

	public WatchlistController(WatchlistService watchlistService) {
		this.watchlistService = watchlistService;
	}

	@GetMapping
	public List<String> getWatchlist(Principal principal) {
		return watchlistService.getWatchlistMovieIds(principal.getName());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public List<String> addToWatchlist(@Valid @RequestBody WatchlistRequest request, Principal principal) {
		return watchlistService.addMovie(principal.getName(), request.movieId());
	}

	@DeleteMapping("/{movieId}")
	public List<String> removeFromWatchlist(@PathVariable String movieId, Principal principal) {
		return watchlistService.removeMovie(principal.getName(), movieId);
	}
}
