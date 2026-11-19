package com.sip.internhub.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.sip.internhub.entities.Role;
import com.sip.internhub.services.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/roles")
public class RoleRestController {

	@Autowired
	private RoleService roleService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<Role> getAllRoles() {
		return roleService.findAll();
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Role createRole(@Valid @RequestBody Role role) {
		return roleService.create(role);
	}

	@DeleteMapping("/{roleId}")
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteRole(@PathVariable Long roleId) {
		roleService.delete(roleId);
	}
}
