package com.sip.internhub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sip.internhub.entities.Role;
import com.sip.internhub.exceptions.BadRequestException;
import com.sip.internhub.exceptions.ResourceNotFoundException;
import com.sip.internhub.repositories.RoleRepository;

@Service
public class RoleService {

	@Autowired
	private RoleRepository roleRepository;

	public List<Role> findAll() {
		return roleRepository.findAll();
	}

	public Role create(Role role) {
		if (roleRepository.findByLibelle(role.getLibelle()).isPresent()) {
			throw new BadRequestException("Le rôle " + role.getLibelle() + " existe déjà");
		}
		return roleRepository.save(role);
	}

	public void delete(Long id) {
		Role role = roleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Rôle " + id + " introuvable"));
		roleRepository.delete(role);
	}
}
