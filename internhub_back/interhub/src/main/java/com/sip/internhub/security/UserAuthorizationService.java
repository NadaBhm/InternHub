package com.sip.internhub.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.sip.internhub.repositories.UserRepository;
import com.sip.internhub.repositories.CandidatureRepository;

@Component("userAuthorization")
public class UserAuthorizationService {

	private final UserRepository userRepository;
	private final CandidatureRepository candidatureRepository;

	public UserAuthorizationService(UserRepository userRepository, CandidatureRepository candidatureRepository) {
		this.userRepository = userRepository;
		this.candidatureRepository = candidatureRepository;
	}

	public boolean isCurrentUser(Long userId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		return userRepository.findById(userId)
				.map(user -> user.getEmail().equals(authentication.getName()))
				.orElse(false);
	}

	public boolean isApplicationOwner(Long applicationId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		return candidatureRepository.existsByIdAndUserEmail(applicationId, authentication.getName());
	}
}
