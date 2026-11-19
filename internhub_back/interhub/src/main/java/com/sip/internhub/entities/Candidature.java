package com.sip.internhub.entities;

import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "offre_id" }))
public class Candidature {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate dateCandidature;

	@Enumerated(EnumType.STRING)
	private StatutCandidature statut = StatutCandidature.EN_ATTENTE;

	@NotBlank
	@Size(max = 180)
	private String etablissement;

	@NotBlank
	@Size(max = 120)
	private String niveauEtudes;

	@NotBlank
	@Size(max = 180)
	private String localisation;

	@Size(max = 500)
	private String cvLocation;

	@Size(max = 255)
	private String cvFileName;

	@Size(max = 2000)
	@Column(length = 2000)
	private String motivation;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	@ManyToOne(optional = false)
	@JoinColumn(name = "offre_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Offre offre;

	public Candidature() {
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public LocalDate getDateCandidature() { return dateCandidature; }
	public void setDateCandidature(LocalDate dateCandidature) { this.dateCandidature = dateCandidature; }
	public StatutCandidature getStatut() { return statut; }
	public void setStatut(StatutCandidature statut) { this.statut = statut; }
	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	public Offre getOffre() { return offre; }
	public void setOffre(Offre offre) { this.offre = offre; }
	public String getEtablissement() { return etablissement; }
	public void setEtablissement(String etablissement) { this.etablissement = etablissement; }
	public String getNiveauEtudes() { return niveauEtudes; }
	public void setNiveauEtudes(String niveauEtudes) { this.niveauEtudes = niveauEtudes; }
	public String getLocalisation() { return localisation; }
	public void setLocalisation(String localisation) { this.localisation = localisation; }
	public String getCvLocation() { return cvLocation; }
	public void setCvLocation(String cvLocation) { this.cvLocation = cvLocation; }
	public String getCvFileName() { return cvFileName; }
	public void setCvFileName(String cvFileName) { this.cvFileName = cvFileName; }
	public String getMotivation() { return motivation; }
	public void setMotivation(String motivation) { this.motivation = motivation; }
}
