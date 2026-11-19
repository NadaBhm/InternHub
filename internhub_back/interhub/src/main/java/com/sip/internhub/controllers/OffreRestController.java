package com.sip.internhub.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.sip.internhub.entities.Offre;
import com.sip.internhub.services.OffreService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/offres")
public class OffreRestController {

	@Autowired
	private OffreService offreService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
	public List<Offre> getAllOffres() {
		return offreService.findAll();
	}

	@GetMapping("/{offreId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'ETUDIANT')")
	public Offre getOffre(@PathVariable Long offreId) {
		return offreService.findOne(offreId);
	}

	// ajoute une offre à l'entreprise entrepriseId
	@PostMapping("/{entrepriseId}")
	@PreAuthorize("hasRole('ADMIN')")
	public Offre createOffre(@PathVariable Long entrepriseId, @Valid @RequestBody Offre offre) {
		return offreService.create(entrepriseId, offre);
	}

	@PutMapping("/{offreId}")
	@PreAuthorize("hasRole('ADMIN')")
	public Offre updateOffre(@PathVariable Long offreId, @Valid @RequestBody Offre offre) {
		return offreService.update(offreId, offre);
	}

	@DeleteMapping("/{offreId}")
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteOffre(@PathVariable Long offreId) {
		offreService.delete(offreId);
	}
}
