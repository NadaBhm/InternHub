package com.sip.internhub.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sip.internhub.entities.Candidature;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

	List<Candidature> findByUserId(Long userId);

	boolean existsByUserIdAndOffreId(Long userId, Long offreId);

	boolean existsByIdAndUserEmail(Long id, String email);
}
