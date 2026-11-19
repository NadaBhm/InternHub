package com.sip.internhub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sip.internhub.entities.Entreprise;
import com.sip.internhub.entities.Offre;
import com.sip.internhub.exceptions.ResourceNotFoundException;
import com.sip.internhub.repositories.EntrepriseRepository;
import com.sip.internhub.repositories.OffreRepository;

@Service
public class OffreService {

	@Autowired
	private OffreRepository offreRepository;

	@Autowired
	private EntrepriseRepository entrepriseRepository;

	public List<Offre> findAll() {
		return offreRepository.findAll();
	}

	public Offre findOne(Long id) {
		return offreRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Offre " + id + " introuvable"));
	}

	public Offre create(Long entrepriseId, Offre offre) {
		Entreprise entreprise = entrepriseRepository.findById(entrepriseId)
				.orElseThrow(() -> new ResourceNotFoundException("Entreprise " + entrepriseId + " introuvable"));
		offre.setId(null);
		offre.setEntreprise(entreprise);
		return offreRepository.save(offre);
	}

	public Offre update(Long id, Offre request) {
		Offre offre = findOne(id);
		offre.setTitre(request.getTitre());
		offre.setDescription(request.getDescription());
		offre.setDomaine(request.getDomaine());
		offre.setDuree(request.getDuree());
		offre.setDateLimite(request.getDateLimite());
		return offreRepository.save(offre);
	}

	public void delete(Long id) {
		offreRepository.delete(findOne(id));
	}
}
