package com.sip.internhub.entities;

import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
public class Offre {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Le titre de l'offre est obligatoire")
	private String titre;

	@Column(length = 2000)
	private String description;

	private String domaine;

	// durée du stage en mois
	@Positive(message = "La durée doit être positive")
	private Integer duree;

	private LocalDate dateLimite;

	@ManyToOne(optional = false)
	@JoinColumn(name = "entreprise_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Entreprise entreprise;

	public Offre() {
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getTitre() { return titre; }
	public void setTitre(String titre) { this.titre = titre; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getDomaine() { return domaine; }
	public void setDomaine(String domaine) { this.domaine = domaine; }
	public Integer getDuree() { return duree; }
	public void setDuree(Integer duree) { this.duree = duree; }
	public LocalDate getDateLimite() { return dateLimite; }
	public void setDateLimite(LocalDate dateLimite) { this.dateLimite = dateLimite; }
	public Entreprise getEntreprise() { return entreprise; }
	public void setEntreprise(Entreprise entreprise) { this.entreprise = entreprise; }
}
