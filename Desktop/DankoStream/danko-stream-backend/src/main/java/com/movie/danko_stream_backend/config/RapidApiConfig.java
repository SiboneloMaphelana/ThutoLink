package com.movie.danko_stream_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class RapidApiConfig {

	@Bean
	RestClient rapidApiRestClient(RapidApiProperties properties) {
		return RestClient.builder()
			.baseUrl(properties.baseUrl())
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader("X-RapidAPI-Host", properties.host())
			.defaultHeader("X-RapidAPI-Key", properties.key())
			.build();
	}
}
