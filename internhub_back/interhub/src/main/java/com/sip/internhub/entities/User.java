package com.sip.internhub.entities;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "L'email est obligatoire")
	@Email(message = "Email invalide")
	@Column(unique = true)
	private String email;

	// le mot de passe est accepté en entrée mais jamais renvoyé dans les réponses JSON
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères")
	private String password;

	private String tel;

	@NotBlank(message = "Le nom est obligatoire")
	private String nom;

	private String prenom;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	public User() {
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
	public String getTel() { return tel; }
	public void setTel(String tel) { this.tel = tel; }
	public String getNom() { return nom; }
	public void setNom(String nom) { this.nom = nom; }
	public String getPrenom() { return prenom; }
	public void setPrenom(String prenom) { this.prenom = prenom; }
	public Set<Role> getRoles() { return roles; }
	public void setRoles(Set<Role> roles) { this.roles = roles; }
}
