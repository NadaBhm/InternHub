package com.sip.internhub.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sip.internhub.entities.Candidature;
import com.sip.internhub.entities.Offre;
import com.sip.internhub.entities.StatutCandidature;
import com.sip.internhub.entities.User;
import com.sip.internhub.exceptions.BadRequestException;
import com.sip.internhub.exceptions.ResourceNotFoundException;
import com.sip.internhub.repositories.CandidatureRepository;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class CandidatureService {

	@Autowired
	private CandidatureRepository candidatureRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private OffreService offreService;

	@Autowired
	private CvStorageService cvStorageService;

	public List<Candidature> findAll() {
		return candidatureRepository.findAll();
	}

	public List<Candidature> findByUser(Long userId) {
		return candidatureRepository.findByUserId(userId);
	}

	public Candidature findOne(Long id) {
		return candidatureRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Candidature " + id + " introuvable"));
	}

	/**
	 * Règles de gestion : une seule candidature par offre et par étudiant, et pas de
	 * candidature après la date limite.
	 */
	public Candidature create(Long userId, Long offreId, Candidature details, MultipartFile cv) throws IOException {
		User user = userService.findOne(userId);
		Offre offre = offreService.findOne(offreId);

		if (details.getEtablissement() == null || details.getEtablissement().isBlank()
				|| details.getNiveauEtudes() == null || details.getNiveauEtudes().isBlank()
				|| details.getLocalisation() == null || details.getLocalisation().isBlank()) {
			throw new BadRequestException("L'établissement, le niveau d'études et la localisation sont obligatoires");
		}

		if (candidatureRepository.existsByUserIdAndOffreId(userId, offreId)) {
			throw new BadRequestException("Vous avez déjà candidaté à cette offre");
		}
		if (offre.getDateLimite() != null && offre.getDateLimite().isBefore(LocalDate.now())) {
			throw new BadRequestException("La date limite de cette offre est dépassée");
		}
		CvStorageService.StoredCv storedCv = cvStorageService.store(cv);

		Candidature candidature = new Candidature();
		candidature.setUser(user);
		candidature.setOffre(offre);
		candidature.setDateCandidature(LocalDate.now());
		candidature.setStatut(StatutCandidature.EN_ATTENTE);
		candidature.setEtablissement(details.getEtablissement().trim());
		candidature.setNiveauEtudes(details.getNiveauEtudes().trim());
		candidature.setLocalisation(details.getLocalisation().trim());
		candidature.setCvLocation(storedCv.storageKey());
		candidature.setCvFileName(storedCv.originalFilename());
		candidature.setMotivation(details.getMotivation() == null ? null : details.getMotivation().trim());
		try {
			return candidatureRepository.save(candidature);
		} catch (RuntimeException persistenceFailure) {
			try {
				cvStorageService.delete(storedCv.storageKey());
			} catch (IOException cleanupFailure) {
				persistenceFailure.addSuppressed(cleanupFailure);
			}
			throw persistenceFailure;
		}
	}

	public Candidature updateStatut(Long id, StatutCandidature statut) {
		Candidature candidature = findOne(id);
		candidature.setStatut(statut);
		return candidatureRepository.save(candidature);
	}

	public void delete(Long id) {
		candidatureRepository.delete(findOne(id));
	}
}
