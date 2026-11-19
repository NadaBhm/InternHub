package com.sip.internhub.controllers;

import java.util.List;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import com.sip.internhub.entities.Candidature;
import com.sip.internhub.entities.StatutCandidature;
import com.sip.internhub.services.CandidatureService;
import com.sip.internhub.services.CvStorageService;
import java.io.IOException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/candidatures")
@Validated
public class CandidatureRestController {

	@Autowired
	private CandidatureService candidatureService;

	@Autowired
	private CvStorageService cvStorageService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<Candidature> getAllCandidatures() {
		return candidatureService.findAll();
	}

	// candidatures d'un étudiant
	@GetMapping("/user/{userId}")
	@PreAuthorize("hasRole('ADMIN') or @userAuthorization.isCurrentUser(#userId)")
	public List<Candidature> getCandidaturesByUser(@PathVariable("userId") Long userId) {
		return candidatureService.findByUser(userId);
	}

	// l'étudiant userId candidate à l'offre offreId
	@PostMapping("/{userId}/{offreId}")
	@PreAuthorize("hasRole('ETUDIANT') and @userAuthorization.isCurrentUser(#userId)")
	public Candidature createCandidature(@PathVariable("userId") Long userId,
			@PathVariable("offreId") Long offreId,
			@RequestParam("etablissement") @NotBlank @Size(max = 180) String etablissement,
			@RequestParam("niveauEtudes") @NotBlank @Size(max = 120) String niveauEtudes,
			@RequestParam("localisation") @NotBlank @Size(max = 180) String localisation,
			@RequestParam(name = "motivation", required = false) @Size(max = 2000) String motivation,
			@RequestParam("cv") MultipartFile cv) throws IOException {
		Candidature details = new Candidature();
		details.setEtablissement(etablissement);
		details.setNiveauEtudes(niveauEtudes);
		details.setLocalisation(localisation);
		details.setMotivation(motivation);
		return candidatureService.create(userId, offreId, details, cv);
	}

	@GetMapping("/{candidatureId}/cv")
	@PreAuthorize("hasRole('ADMIN') or @userAuthorization.isApplicationOwner(#candidatureId)")
	public ResponseEntity<Resource> downloadCv(@PathVariable Long candidatureId) throws IOException {
		Candidature application = candidatureService.findOne(candidatureId);
		Path file = cvStorageService.resolve(application.getCvLocation());
		Resource resource = new UrlResource(file.toUri());
		String filename = application.getCvFileName() == null ? "cv.pdf" : application.getCvFileName();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION,
						ContentDisposition.attachment().filename(filename).build().toString())
				.body(resource);
	}

	// changer le statut : /candidatures/1/statut?statut=ACCEPTEE
	@PutMapping("/{candidatureId}/statut")
	@PreAuthorize("hasRole('ADMIN')")
	public Candidature updateStatut(@PathVariable("candidatureId") Long candidatureId,
			@RequestParam("statut") StatutCandidature statut) {
		return candidatureService.updateStatut(candidatureId, statut);
	}

	@DeleteMapping("/{candidatureId}")
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteCandidature(@PathVariable Long candidatureId) {
		candidatureService.delete(candidatureId);
	}
}
