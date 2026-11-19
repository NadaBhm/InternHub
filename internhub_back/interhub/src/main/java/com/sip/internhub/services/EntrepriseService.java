package com.sip.internhub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sip.internhub.entities.Entreprise;
import com.sip.internhub.exceptions.ResourceNotFoundException;
import com.sip.internhub.repositories.EntrepriseRepository;

@Service
public class EntrepriseService {

	@Autowired
	private EntrepriseRepository entrepriseRepository;

	public List<Entreprise> findAll() {
		return entrepriseRepository.findAll();
	}

	public Entreprise findOne(Long id) {
		return entrepriseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Entreprise " + id + " introuvable"));
	}

	public Entreprise create(Entreprise entreprise) {
		entreprise.setId(null);
		return entrepriseRepository.save(entreprise);
	}

	public Entreprise update(Long id, Entreprise request) {
		Entreprise entreprise = findOne(id);
		entreprise.setNom(request.getNom());
		entreprise.setSecteur(request.getSecteur());
		entreprise.setEmail(request.getEmail());
		entreprise.setTel(request.getTel());
		entreprise.setAdresse(request.getAdresse());
		return entrepriseRepository.save(entreprise);
	}

	public void delete(Long id) {
		entrepriseRepository.delete(findOne(id));
	}
}
