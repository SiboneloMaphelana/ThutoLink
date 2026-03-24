package com.movie.danko_stream_backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.movie.danko_stream_backend.model.MovieDto;

@Service
public class MoviesDatabaseService {

	private static final String POSTER_FALLBACK = "https://placehold.co/800x1200/111827/f9fafb?text=No+Poster";
	private static final String BACKDROP_FALLBACK = "https://placehold.co/1400x788/0f172a/e2e8f0?text=No+Backdrop";

	private final RestClient rapidApiRestClient;

	public MoviesDatabaseService(RestClient rapidApiRestClient) {
		this.rapidApiRestClient = rapidApiRestClient;
	}

	public List<MovieDto> getMovies(String genre, int limit) {
		int safeLimit = Math.max(1, Math.min(limit, 50));
		String uri = UriComponentsBuilder.fromPath("/titles")
			.queryParam("list", "most_pop_movies")
			.queryParam("limit", safeLimit)
			.queryParamIfPresent("genre", StringUtils.hasText(genre) ? java.util.Optional.of(genre) : java.util.Optional.empty())
			.build()
			.toUriString();

		JsonNode payload = getJson(uri);
		List<MovieDto> movies = new ArrayList<>();

		for (JsonNode item : resultsArray(payload)) {
			movies.add(toMovieDto(item));
		}

		return movies.stream()
			.sorted(Comparator.comparingDouble(MovieDto::rating).reversed())
			.toList();
	}

	public MovieDto getMovieById(String id) {
		JsonNode payload = getJson("/titles/" + id);
		JsonNode item = firstResult(payload);
		if (item == null || item.isMissingNode() || item.isNull()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found");
		}
		return toMovieDto(item);
	}

	private JsonNode getJson(String uri) {
		try {
			JsonNode body = rapidApiRestClient.get()
				.uri(uri)
				.retrieve()
				.body(JsonNode.class);

			if (body == null) {
				throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from MoviesDatabase");
			}

			return body;
		} catch (RestClientException exception) {
			throw new ResponseStatusException(
				HttpStatus.BAD_GATEWAY,
				"MoviesDatabase request failed. Check RapidAPI configuration.",
				exception
			);
		}
	}

	private JsonNode resultsArray(JsonNode payload) {
		JsonNode results = payload.path("results");
		return results.isArray() ? results : payload;
	}

	private JsonNode firstResult(JsonNode payload) {
		JsonNode results = payload.path("results");
		if (results.isArray() && !results.isEmpty()) {
			return results.get(0);
		}
		return results.isObject() ? results : payload;
	}

	private MovieDto toMovieDto(JsonNode item) {
		List<String> genres = textValues(item.at("/genres/genres"), "text");
		List<String> cast = textValues(item.path("cast"), "name");
		if (cast.isEmpty()) {
			cast = textValues(item.path("stars"), "name");
		}

		String title = firstNonBlank(
			text(item.at("/titleText/text")),
			text(item.path("title")),
			text(item.path("originalTitle"))
		);
		String plot = firstNonBlank(
			text(item.at("/plot/plotText/plainText")),
			text(item.path("plot")),
			text(item.path("description"))
		);
		String primaryImage = firstNonBlank(
			text(item.at("/primaryImage/url")),
			text(item.at("/images/0/url"))
		);
		String backdrop = firstNonBlank(
			text(item.at("/primaryImage/url")),
			text(item.at("/images/1/url"))
		);
		double rating = firstNumber(
			item.at("/ratingsSummary/aggregateRating"),
			item.path("rating")
		);
		int voteCount = (int) Math.round(firstNumber(
			item.at("/ratingsSummary/voteCount"),
			item.path("numVotes")
		));
		int runtime = firstInt(
			item.path("runtimeMinutes"),
			item.at("/runtime/displayableProperty/value/plainText"),
			item.path("runtime")
		);
		int year = firstInt(
			item.at("/releaseYear/year"),
			item.at("/releaseDate/year"),
			item.path("year")
		);
		String language = firstNonBlank(
			joinTextValues(item.path("spokenLanguages"), "text"),
			joinTextValues(item.path("languages"), "name"),
			"Unknown"
		);
		String director = firstNonBlank(
			joinCredits(item.path("directors")),
			joinCredits(item.path("crew"), "director"),
			"Not available"
		);

		return new MovieDto(
			firstNonBlank(text(item.path("id")), text(item.path("_id")), slugify(title)),
			firstNonBlank(title, "Untitled"),
			year > 0 ? year : 0,
			runtime > 0 ? runtime : 100,
			deriveIntensity(genres, rating),
			deriveMoods(genres),
			genres,
			firstNonBlank(plot, "Overview not available yet."),
			firstNonBlank(primaryImage, POSTER_FALLBACK),
			firstNonBlank(backdrop, primaryImage, BACKDROP_FALLBACK),
			rating > 0 ? rating : 6.5,
			voteCount > 0 ? voteCount : 0,
			language,
			director,
			cast.stream().limit(5).toList(),
			List.of("Provider info unavailable in MoviesDatabase")
		);
	}

	private List<String> deriveMoods(List<String> genres) {
		Set<String> moods = new LinkedHashSet<>();
		for (String genre : genres) {
			String normalized = genre.toLowerCase(Locale.ROOT);
			switch (normalized) {
				case "comedy", "animation", "family", "music" -> {
					moods.add("uplifting");
					moods.add("escapist");
				}
				case "romance" -> {
					moods.add("romantic");
					moods.add("thoughtful");
				}
				case "drama", "history", "biography", "documentary" -> moods.add("thoughtful");
				case "thriller", "horror", "crime", "mystery" -> {
					moods.add("tense");
					moods.add("dark");
				}
				case "action", "adventure", "fantasy", "sci-fi", "science fiction" -> {
					moods.add("escapist");
					moods.add("tense");
				}
				default -> {
				}
			}
		}

		if (moods.isEmpty()) {
			moods.add("thoughtful");
		}

		return List.copyOf(moods);
	}

	private String deriveIntensity(List<String> genres, double rating) {
		Set<String> normalizedGenres = genres.stream()
			.map((genre) -> genre.toLowerCase(Locale.ROOT))
			.collect(Collectors.toSet());

		if (normalizedGenres.stream().anyMatch((genre) -> List.of("action", "thriller", "horror", "crime").contains(genre))) {
			return "intense";
		}

		if (normalizedGenres.stream().anyMatch((genre) -> List.of("family", "animation", "comedy", "romance").contains(genre))) {
			return "gentle";
		}

		return rating >= 7.5 ? "balanced" : "gentle";
	}

	private List<String> textValues(JsonNode arrayNode, String fieldName) {
		if (!arrayNode.isArray()) {
			return List.of();
		}

		List<String> values = new ArrayList<>();
		for (JsonNode node : arrayNode) {
			String value = fieldName == null ? text(node) : firstNonBlank(text(node.path(fieldName)), text(node.at("/nameText/text")));
			if (StringUtils.hasText(value)) {
				values.add(value);
			}
		}
		return values;
	}

	private String joinTextValues(JsonNode arrayNode, String fieldName) {
		return String.join(", ", textValues(arrayNode, fieldName));
	}

	private String joinCredits(JsonNode creditsNode) {
		List<String> names = new ArrayList<>();
		if (creditsNode.isArray()) {
			for (JsonNode entry : creditsNode) {
				names.addAll(textValues(entry.path("credits"), "name"));
				names.addAll(textValues(entry.path("names"), "name"));
			}
		}
		return names.stream().distinct().collect(Collectors.joining(", "));
	}

	private String joinCredits(JsonNode creditsNode, String filterRole) {
		if (!creditsNode.isArray()) {
			return "";
		}

		List<String> names = new ArrayList<>();
		for (JsonNode entry : creditsNode) {
			String role = text(entry.path("category"));
			if (!filterRole.equalsIgnoreCase(role)) {
				continue;
			}
			names.add(firstNonBlank(text(entry.path("name")), text(entry.at("/nameText/text"))));
		}
		return names.stream()
			.filter(StringUtils::hasText)
			.distinct()
			.collect(Collectors.joining(", "));
	}

	private String text(JsonNode node) {
		return node != null && node.isValueNode() ? node.asText() : "";
	}

	private int firstInt(JsonNode... nodes) {
		for (JsonNode node : nodes) {
			if (node == null || node.isMissingNode() || node.isNull()) {
				continue;
			}
			if (node.isInt() || node.isLong()) {
				return node.asInt();
			}
			if (node.isTextual()) {
				String value = node.asText().replaceAll("[^0-9]", "");
				if (StringUtils.hasText(value)) {
					return Integer.parseInt(value);
				}
			}
		}
		return 0;
	}

	private double firstNumber(JsonNode... nodes) {
		for (JsonNode node : nodes) {
			if (node == null || node.isMissingNode() || node.isNull()) {
				continue;
			}
			if (node.isNumber()) {
				return node.asDouble();
			}
			if (node.isTextual()) {
				try {
					return Double.parseDouble(node.asText());
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return 0;
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			if (StringUtils.hasText(value)) {
				return value;
			}
		}
		return "";
	}

	private String slugify(String value) {
		if (!StringUtils.hasText(value)) {
			return "unknown-title";
		}
		return value.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9]+", "-")
			.replaceAll("(^-|-$)", "");
	}
}
