package com.movie.danko_stream_backend.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "watchlist_items",
	uniqueConstraints = @UniqueConstraint(name = "uk_watchlist_user_movie", columnNames = { "username", "movie_id" })
)
public class WatchlistItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String username;

	@Column(name = "movie_id", nullable = false, length = 100)
	private String movieId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected WatchlistItem() {
	}

	public WatchlistItem(String username, String movieId) {
		this.username = username;
		this.movieId = movieId;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getMovieId() {
		return movieId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
