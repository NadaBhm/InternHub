package com.sip.internhub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sip.internhub.entities.Entreprise;

@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
}
