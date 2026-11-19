package com.sip.internhub.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.sip.internhub.entities.Entreprise;
import com.sip.internhub.services.EntrepriseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/entreprises")
public class EntrepriseRestController {

	@Autowired
	private EntrepriseService entrepriseService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
	public List<Entreprise> getAllEntreprises() {
		return entrepriseService.findAll();
	}

	@GetMapping("/{entrepriseId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
	public Entreprise getEntreprise(@PathVariable Long entrepriseId) {
		return entrepriseService.findOne(entrepriseId);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public Entreprise createEntreprise(@Valid @RequestBody Entreprise entreprise) {
		return entrepriseService.create(entreprise);
	}

	@PutMapping("/{entrepriseId}")
	@PreAuthorize("hasRole('ADMIN')")
	public Entreprise updateEntreprise(@PathVariable Long entrepriseId, @Valid @RequestBody Entreprise entreprise) {
		return entrepriseService.update(entrepriseId, entreprise);
	}

	@DeleteMapping("/{entrepriseId}")
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteEntreprise(@PathVariable Long entrepriseId) {
		entrepriseService.delete(entrepriseId);
	}
}
