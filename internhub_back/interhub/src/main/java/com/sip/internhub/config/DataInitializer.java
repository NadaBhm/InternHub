package com.sip.internhub.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sip.internhub.entities.Role;
import com.sip.internhub.entities.Candidature;
import com.sip.internhub.entities.Entreprise;
import com.sip.internhub.entities.Offre;
import com.sip.internhub.entities.StatutCandidature;
import com.sip.internhub.entities.User;
import com.sip.internhub.repositories.CandidatureRepository;
import com.sip.internhub.repositories.EntrepriseRepository;
import com.sip.internhub.repositories.OffreRepository;
import com.sip.internhub.repositories.RoleRepository;
import com.sip.internhub.repositories.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Crée les deux rôles de l'application au démarrage s'ils n'existent pas.
 */
@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initRoles(RoleRepository roleRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.bootstrap-admin.email:}") String adminEmail,
			@Value("${app.bootstrap-admin.password:}") String adminPassword,
			@Value("${app.demo-data:true}") boolean demoData,
			EntrepriseRepository entrepriseRepository, OffreRepository offreRepository,
			CandidatureRepository candidatureRepository) {
		return args -> {
			for (String libelle : new String[] { "ADMIN", "ETUDIANT" }) {
				if (roleRepository.findByLibelle(libelle).isEmpty()) {
					roleRepository.save(new Role(libelle));
				}
			}

			for (User user : userRepository.findAll()) {
				String password = user.getPassword();
				if (password != null && !isBcryptPassword(password)) {
					user.setPassword(passwordEncoder.encode(password));
					userRepository.save(user);
				}
			}

			if (adminEmail.isBlank() != adminPassword.isBlank()) {
				throw new IllegalStateException(
						"Set both INTERNHUB_BOOTSTRAP_ADMIN_EMAIL and INTERNHUB_BOOTSTRAP_ADMIN_PASSWORD");
			}
			if (!adminEmail.isBlank() && !adminPassword.isBlank()) {
				if (adminPassword.length() < 8) {
					throw new IllegalStateException("INTERNHUB_BOOTSTRAP_ADMIN_PASSWORD must contain at least 8 characters");
				}
				User admin = userRepository.findByEmail(adminEmail.trim().toLowerCase()).orElseGet(User::new);
				admin.setEmail(adminEmail.trim().toLowerCase());
				admin.setNom(admin.getNom() == null ? "Administrateur" : admin.getNom());
				if (admin.getPassword() == null || admin.getPassword().isBlank()) {
					admin.setPassword(passwordEncoder.encode(adminPassword));
				}
				admin.getRoles().add(roleRepository.findByLibelle("ADMIN")
						.orElseThrow(() -> new IllegalStateException("Le rôle ADMIN n'existe pas")));
				userRepository.save(admin);
			}

			if (demoData) {
				Role studentRole = roleRepository.findByLibelle("ETUDIANT")
						.orElseThrow(() -> new IllegalStateException("Le rôle ETUDIANT n'existe pas"));

				List<Entreprise> companies = new ArrayList<>();
				companies.add(createCompany(entrepriseRepository, "NovaSoft Tunisia",
						"Informatique", "contact@novasoft.demo", "71123456", "Lac 2, Tunis"));
				companies.add(createCompany(entrepriseRepository, "GreenAxis",
						"Énergies renouvelables", "jobs@greenaxis.demo", "71234567", "Sousse, Tunisie"));
				companies.add(createCompany(entrepriseRepository, "Finora Labs",
						"Fintech", "rh@finora.demo", "71345678", "Les Berges du Lac, Tunis"));
				companies.add(createCompany(entrepriseRepository, "PixelWave",
						"Digital & Design", "stage@pixelwave.demo", "71456789", "Centre-ville, Sfax"));
				companies.add(createCompany(entrepriseRepository, "BioHarvest",
						"AgriTech", "careers@bioharvest.demo", "71567890", "Kairouan, Tunisie"));

				List<Offre> offers = new ArrayList<>();
				offers.add(createOffer(offreRepository, companies.get(0), "Stage Développeur Full Stack",
						"Développer des API Spring Boot et interfaces Angular pour une plateforme SaaS.",
						"Génie logiciel", 6, LocalDate.now().plusMonths(4)));
				offers.add(createOffer(offreRepository, companies.get(0), "Stage Data Engineer",
						"Construire des pipelines de données et analyser des jeux de données volumineux.",
						"Data Engineering", 5, LocalDate.now().plusMonths(3)));
				offers.add(createOffer(offreRepository, companies.get(1), "Stage Ingénieur Énergies Solaires",
						"Assister aux études de dimensionnement de centrales solaires.",
						"Énergies renouvelables", 4, LocalDate.now().plusMonths(5)));
				offers.add(createOffer(offreRepository, companies.get(2), "Stage Cybersécurité",
					"Participer aux tests de sécurité et à la surveillance des applications financières.",
						"Cybersécurité", 6, LocalDate.now().plusMonths(2)));
				offers.add(createOffer(offreRepository, companies.get(2), "Stage Développement Mobile",
					"Créer des fonctionnalités pour une application fintech cross-platform.",
						"Mobile", 5, LocalDate.now().plusMonths(4)));
				offers.add(createOffer(offreRepository, companies.get(3), "Stage UX/UI Designer",
					"Concevoir des parcours utilisateurs et réaliser des prototypes interactifs.",
						"UX/UI", 3, LocalDate.now().plusMonths(3)));
				offers.add(createOffer(offreRepository, companies.get(3), "Stage Référencement Digital",
					"Optimiser le contenu, les campagnes et les performances SEO.",
					"Marketing digital", 4, LocalDate.now().plusMonths(2)));
				offers.add(createOffer(offreRepository, companies.get(4), "Stage IoT & Agriculture",
					"Développer des capteurs connectés pour le suivi des cultures.",
					"Internet des objets", 6, LocalDate.now().plusMonths(5)));

				List<User> students = new ArrayList<>();
				students.add(createStudent(userRepository, passwordEncoder, studentRole,
						"nada.bouhachem@demo.tn", "Bouhachem", "Nada", "21413874"));
				students.add(createStudent(userRepository, passwordEncoder, studentRole,
						"youssef.gharbi@demo.tn", "Gharbi", "Youssef", "22555111"));
				students.add(createStudent(userRepository, passwordEncoder, studentRole,
						"ines.khaled@demo.tn", "Khaled", "Ines", "23444222"));
				students.add(createStudent(userRepository, passwordEncoder, studentRole,
						"omar.trabelsi@demo.tn", "Trabelsi", "Omar", "24666333"));

				createApplication(candidatureRepository, students.get(0), offers.get(0),
						"École Nationale des Sciences de l’Informatique", "3ème année ingénieur",
						"Tunis, Tunisie", "https://example.demo/cv/nada.pdf",
						"Motivée par le développement full stack et les produits SaaS.", StatutCandidature.EN_ATTENTE);
				createApplication(candidatureRepository, students.get(0), offers.get(3),
						"École Nationale des Sciences de l’Informatique", "3ème année ingénieur",
						"Tunis, Tunisie", "https://example.demo/cv/nada.pdf",
						"Intérêt pour la sécurité des applications et les audits.", StatutCandidature.ACCEPTEE);
				createApplication(candidatureRepository, students.get(1), offers.get(1),
						"Institut Supérieur d’Informatique", "Master 1",
						"Sousse, Tunisie", "https://example.demo/cv/youssef.pdf",
						"Expérience universitaire avec Spark et Python.", StatutCandidature.EN_ATTENTE);
				createApplication(candidatureRepository, students.get(2), offers.get(5),
					"Institut Supérieur des Arts Multimédia", "Licence 3",
						"Tunis, Tunisie", "https://example.demo/cv/ines.pdf",
						"Passionnée par la recherche utilisateur et le prototypage.", StatutCandidature.REFUSEE);
				createApplication(candidatureRepository, students.get(3), offers.get(7),
						"École Nationale d’Ingénieurs de Tunis", "2ème année ingénieur",
						"Kairouan, Tunisie", "https://example.demo/cv/omar.pdf",
						"Soucieux de mettre l’IoT au service de l’agriculture.", StatutCandidature.EN_ATTENTE);
				createApplication(candidatureRepository, students.get(3), offers.get(4),
						"École Nationale d’Ingénieurs de Tunis", "2ème année ingénieur",
						"Tunis, Tunisie", "https://example.demo/cv/omar.pdf",
						"Mobile, Kotlin et Flutter sont des domaines que je souhaite approfondir.", StatutCandidature.ACCEPTEE);
			}
		};
	}

	private boolean isBcryptPassword(String password) {
		return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
	}

	private Entreprise createCompany(EntrepriseRepository repository, String name, String sector,
			String email, String phone, String address) {
		return repository.findAll().stream()
				.filter(company -> company.getNom().equalsIgnoreCase(name))
				.findFirst()
				.orElseGet(() -> {
					Entreprise company = new Entreprise();
					company.setNom(name);
					company.setSecteur(sector);
					company.setEmail(email);
					company.setTel(phone);
					company.setAdresse(address);
					return repository.save(company);
				});
	}

	private Offre createOffer(OffreRepository repository, Entreprise company, String title,
			String description, String domain, int duration, LocalDate deadline) {
		return repository.findAll().stream()
				.filter(offer -> offer.getTitre().equalsIgnoreCase(title)
						&& offer.getEntreprise().getId().equals(company.getId()))
				.findFirst()
				.orElseGet(() -> {
					Offre offer = new Offre();
					offer.setTitre(title);
					offer.setDescription(description);
					offer.setDomaine(domain);
					offer.setDuree(duration);
					offer.setDateLimite(deadline);
					offer.setEntreprise(company);
					return repository.save(offer);
				});
	}

	private User createStudent(UserRepository repository, PasswordEncoder passwordEncoder, Role studentRole,
			String email, String lastName, String firstName, String phone) {
		return repository.findByEmail(email).orElseGet(() -> {
			User student = new User();
			student.setEmail(email);
			student.setPassword(passwordEncoder.encode("StudentDemo123"));
			student.setNom(lastName);
			student.setPrenom(firstName);
			student.setTel(phone);
			student.getRoles().add(studentRole);
			return repository.save(student);
		});
	}

	private void createApplication(CandidatureRepository repository, User student, Offre offer,
			String school, String level, String location, String cvLocation,
			String motivation, StatutCandidature status) {
		if (repository.existsByUserIdAndOffreId(student.getId(), offer.getId())) {
			return;
		}
		Candidature application = new Candidature();
		application.setUser(student);
		application.setOffre(offer);
		application.setDateCandidature(LocalDate.now().minusDays(student.getId() % 12));
		application.setStatut(status);
		application.setEtablissement(school);
		application.setNiveauEtudes(level);
		application.setLocalisation(location);
		application.setCvLocation(cvLocation);
		application.setMotivation(motivation);
		repository.save(application);
	}
}
