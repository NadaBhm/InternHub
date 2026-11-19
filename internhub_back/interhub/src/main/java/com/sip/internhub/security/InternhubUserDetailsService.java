package com.sip.internhub.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sip.internhub.repositories.UserRepository;

@Service
public class InternhubUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public InternhubUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return userRepository.findByEmail(email)
				.map(user -> org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
						.password(user.getPassword())
						.authorities(user.getRoles().stream()
								.map(role -> "ROLE_" + role.getLibelle().toUpperCase())
								.toArray(String[]::new))
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
	}
}
