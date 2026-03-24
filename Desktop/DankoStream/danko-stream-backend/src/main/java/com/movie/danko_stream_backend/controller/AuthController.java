package com.movie.danko_stream_backend.controller;

import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.movie.danko_stream_backend.model.AuthRequest;
import com.movie.danko_stream_backend.model.AuthResponse;
import com.movie.danko_stream_backend.model.RegisterRequest;
import com.movie.danko_stream_backend.model.UserAccount;
import com.movie.danko_stream_backend.service.UserRegistrationService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final UserRegistrationService userRegistrationService;

	public AuthController(
		AuthenticationManager authenticationManager,
		UserDetailsService userDetailsService,
		UserRegistrationService userRegistrationService
	) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.userRegistrationService = userRegistrationService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(
		@Valid @RequestBody RegisterRequest request,
		HttpServletRequest httpRequest
	) {
		UserAccount created = userRegistrationService.register(request.username(), request.password());
		if (created == null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(created.getUsername());
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
			userDetails.getUsername(),
			null,
			userDetails.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		httpRequest.getSession(true).setAttribute(
			HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
			SecurityContextHolder.getContext()
		);

		return ResponseEntity.status(HttpStatus.CREATED).body(toAuthResponse(authentication));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
		@Valid @RequestBody AuthRequest request,
		HttpServletRequest httpRequest
	) {
		String username = request.username().trim().toLowerCase(Locale.ROOT);
		try {
			Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(username, request.password())
			);

			SecurityContextHolder.getContext().setAuthentication(authentication);
			httpRequest.getSession(true).setAttribute(
				HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
				SecurityContextHolder.getContext()
			);

			return ResponseEntity.ok(toAuthResponse(authentication));
		} catch (AuthenticationException exception) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@GetMapping("/me")
	public ResponseEntity<AuthResponse> me(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated() || isAnonymous(authentication)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		return ResponseEntity.ok(toAuthResponse(authentication));
	}

	private AuthResponse toAuthResponse(Authentication authentication) {
		List<String> roles = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.toList();

		return new AuthResponse(true, authentication.getName(), roles);
	}

	private boolean isAnonymous(Authentication authentication) {
		return authentication.getAuthorities().stream()
			.anyMatch((authority) -> "ROLE_ANONYMOUS".equals(authority.getAuthority()));
	}
}
