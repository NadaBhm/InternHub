package com.sip.internhub.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sip.internhub.entities.Role;
import com.sip.internhub.entities.User;
import com.sip.internhub.exceptions.BadRequestException;
import com.sip.internhub.exceptions.ResourceNotFoundException;
import com.sip.internhub.repositories.RoleRepository;
import com.sip.internhub.repositories.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public List<User> findAll() {
		return userRepository.findAll();
	}

	public User findOne(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur " + id + " introuvable"));
	}

	/**
	 * Retrouve les rôles en base à partir des id/libellés reçus. Sans rôle précisé,
	 * l'utilisateur reçoit le rôle ETUDIANT.
	 */
	private Set<Role> resolveRoles(Set<Role> requested) {
		Set<Role> result = new HashSet<>();
		if (requested != null) {
			for (Role r : requested) {
				Role found = null;
				if (r.getId() != null) {
					found = roleRepository.findById(r.getId()).orElse(null);
				} else if (r.getLibelle() != null) {
					found = roleRepository.findByLibelle(r.getLibelle()).orElse(null);
				}
				if (found == null) {
					throw new BadRequestException("Rôle inconnu : " + (r.getId() != null ? r.getId() : r.getLibelle()));
				}
				result.add(found);
			}
		}
		if (result.isEmpty()) {
			result.add(roleRepository.findByLibelle("ETUDIANT")
					.orElseThrow(() -> new ResourceNotFoundException("Le rôle ETUDIANT n'existe pas")));
		}
		return result;
	}

	public User create(User user) {
		if (user.getPassword() == null || user.getPassword().isBlank()) {
			throw new BadRequestException("Le mot de passe est obligatoire");
		}
		user.setEmail(user.getEmail().trim().toLowerCase());
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new BadRequestException("Un utilisateur avec cet email existe déjà");
		}
		user.setId(null);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRoles(resolveRoles(user.getRoles()));
		return userRepository.save(user);
	}

	public User update(Long id, User request, boolean allowRoleChange) {
		User user = findOne(id);
		request.setEmail(request.getEmail().trim().toLowerCase());
		if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
			throw new BadRequestException("Un utilisateur avec cet email existe déjà");
		}
		user.setEmail(request.getEmail());
		user.setNom(request.getNom());
		user.setPrenom(request.getPrenom());
		user.setTel(request.getTel());
		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
		}
		if (allowRoleChange && request.getRoles() != null && !request.getRoles().isEmpty()) {
			user.setRoles(resolveRoles(request.getRoles()));
		}
		return userRepository.save(user);
	}

	public void delete(Long id) {
		userRepository.delete(findOne(id));
	}
}
