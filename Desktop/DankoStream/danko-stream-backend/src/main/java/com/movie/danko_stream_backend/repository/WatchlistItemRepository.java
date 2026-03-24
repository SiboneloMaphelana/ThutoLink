package com.movie.danko_stream_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.movie.danko_stream_backend.model.WatchlistItem;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {

	List<WatchlistItem> findAllByUsernameOrderByCreatedAtDesc(String username);

	boolean existsByUsernameAndMovieId(String username, String movieId);

	void deleteByUsernameAndMovieId(String username, String movieId);
}
