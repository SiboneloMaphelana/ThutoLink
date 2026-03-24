package com.movie.danko_stream_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DankoStreamBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DankoStreamBackendApplication.class, args);
	}

}
