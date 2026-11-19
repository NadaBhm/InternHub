package com.sip.internhub.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Le libellé du rôle est obligatoire")
	@Column(unique = true)
	private String libelle;

	public Role() {
	}

	public Role(String libelle) {
		this.libelle = libelle;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getLibelle() { return libelle; }
	public void setLibelle(String libelle) { this.libelle = libelle; }
}
