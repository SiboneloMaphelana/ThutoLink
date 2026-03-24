package com.movie.danko_stream_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.movie.danko_stream_backend.model.WatchlistItem;
import com.movie.danko_stream_backend.repository.WatchlistItemRepository;

@Service
public class WatchlistService {

	private final WatchlistItemRepository watchlistItemRepository;

	public WatchlistService(WatchlistItemRepository watchlistItemRepository) {
		this.watchlistItemRepository = watchlistItemRepository;
	}

	@Transactional(readOnly = true)
	public List<String> getWatchlistMovieIds(String username) {
		return watchlistItemRepository.findAllByUsernameOrderByCreatedAtDesc(username).stream()
			.map(WatchlistItem::getMovieId)
			.toList();
	}

	@Transactional
	public List<String> addMovie(String username, String movieId) {
		if (!watchlistItemRepository.existsByUsernameAndMovieId(username, movieId)) {
			watchlistItemRepository.save(new WatchlistItem(username, movieId));
		}
		return getWatchlistMovieIds(username);
	}

	@Transactional
	public List<String> removeMovie(String username, String movieId) {
		watchlistItemRepository.deleteByUsernameAndMovieId(username, movieId);
		return getWatchlistMovieIds(username);
	}
}
