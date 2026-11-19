package com.sip.internhub.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Entreprise {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Le nom de l'entreprise est obligatoire")
	private String nom;

	private String secteur;

	@Email(message = "Email invalide")
	private String email;

	private String tel;

	private String adresse;

	public Entreprise() {
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getNom() { return nom; }
	public void setNom(String nom) { this.nom = nom; }
	public String getSecteur() { return secteur; }
	public void setSecteur(String secteur) { this.secteur = secteur; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getTel() { return tel; }
	public void setTel(String tel) { this.tel = tel; }
	public String getAdresse() { return adresse; }
	public void setAdresse(String adresse) { this.adresse = adresse; }
}
