package com.movie.danko_stream_backend.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.movie.danko_stream_backend.model.UserAccount;
import com.movie.danko_stream_backend.repository.UserAccountRepository;

@Service
public class UserRegistrationService {

	private final UserAccountRepository userAccountRepository;
	private final PasswordEncoder passwordEncoder;

	public UserRegistrationService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
		this.userAccountRepository = userAccountRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserAccount register(String rawUsername, String rawPassword) {
		String username = rawUsername.trim().toLowerCase(Locale.ROOT);
		if (userAccountRepository.existsByUsername(username)) {
			return null;
		}
		UserAccount account = new UserAccount(username, passwordEncoder.encode(rawPassword));
		return userAccountRepository.save(account);
	}
}
