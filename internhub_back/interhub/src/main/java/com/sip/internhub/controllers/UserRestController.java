package com.sip.internhub.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.sip.internhub.entities.User;
import com.sip.internhub.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserRestController {

	@Autowired
	private UserService userService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<User> getAllUsers() {
		return userService.findAll();
	}

	@GetMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN') or @userAuthorization.isCurrentUser(#userId)")
	public User getUser(@PathVariable("userId") Long userId) {
		return userService.findOne(userId);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public User createUser(@Valid @RequestBody User user) {
		return userService.create(user);
	}

	@PutMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN') or @userAuthorization.isCurrentUser(#userId)")
	public User updateUser(@PathVariable("userId") Long userId, @Valid @RequestBody User user,
			@AuthenticationPrincipal UserDetails principal) {
		return userService.update(userId, user, principal.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
	}

	@DeleteMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteUser(@PathVariable Long userId) {
		userService.delete(userId);
	}
}
