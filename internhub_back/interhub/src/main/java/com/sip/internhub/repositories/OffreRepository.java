package com.sip.internhub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sip.internhub.entities.Offre;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {
}
