package com.sip.internhub.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sip.internhub.entities.Role;
import com.sip.internhub.entities.User;
import com.sip.internhub.exceptions.BadRequestException;
import com.sip.internhub.repositories.RoleRepository;
import com.sip.internhub.repositories.UserRepository;
import com.sip.internhub.security.JwtService;
import com.sip.internhub.services.RegistrationNotificationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RegistrationNotificationService notificationService;

	public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
			RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
			RegistrationNotificationService notificationService) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.notificationService = notificationService;
	}

	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody RegistrationRequest request) {
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new BadRequestException("Un utilisateur avec cet email existe déjà");
		}
		Role studentRole = roleRepository.findByLibelle("ETUDIANT")
				.orElseThrow(() -> new IllegalStateException("Le rôle ETUDIANT n'existe pas"));
		User user = new User();
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setNom(request.nom().trim());
		user.setPrenom(request.prenom() == null ? null : request.prenom().trim());
		user.setTel(request.tel() == null ? null : request.tel().trim());
		user.getRoles().add(studentRole);
		User saved = userRepository.save(user);
		notificationService.notifyNewUser(saved);
		return new AuthResponse(jwtService.generateToken(saved.getEmail()), saved);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
			User user = userRepository.findByEmail(authentication.getName())
					.orElseThrow(() -> new IllegalStateException("Utilisateur authentifié introuvable"));
			return new AuthResponse(jwtService.generateToken(user.getEmail()), user);
		} catch (AuthenticationException exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
		}
	}

	public record RegistrationRequest(
			@NotBlank @Email String email,
			@NotBlank @Size(min = 8, max = 72) String password,
			@NotBlank @Size(max = 100) String nom,
			@Size(max = 100) String prenom,
			@Size(max = 40) String tel) { }

	public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) { }

	public record AuthResponse(String token, User user) { }
}
