package com.movie.danko_stream_backend.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.movie.danko_stream_backend.repository.UserAccountRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private final UserAccountRepository userAccountRepository;

	public DatabaseUserDetailsService(UserAccountRepository userAccountRepository) {
		this.userAccountRepository = userAccountRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String normalized = username.trim().toLowerCase();
		return userAccountRepository.findByUsername(normalized)
			.map((account) -> User.withUsername(account.getUsername())
				.password(account.getPasswordHash())
				.roles("USER")
				.build())
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}
