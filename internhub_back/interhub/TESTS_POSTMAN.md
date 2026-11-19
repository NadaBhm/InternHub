# Tests Postman : InternHub (JWT)

URL de base : http://localhost:8080 — Content-Type : application/json pour les POST et PUT.
Les rôles ADMIN et ETUDIANT sont créés automatiquement. Configurez `INTERNHUB_BOOTSTRAP_ADMIN_EMAIL`,
`INTERNHUB_BOOTSTRAP_ADMIN_PASSWORD` et `INTERNHUB_JWT_SECRET` avant le démarrage.
Après connexion, utilisez `Authorization: Bearer <token>` pour les endpoints protégés.

## Authentification

| Méthode | URL | Corps JSON | Résultat attendu |
|---------|-----|------------|------------------|
| POST | /auth/register | `{"email":"nada@ensi.tn","password":"motdepasse-securise","nom":"Bouhachem","prenom":"Nada","tel":"21413874"}` | 200, rôle ETUDIANT uniquement, jeton JWT et profil |
| POST | /auth/login | `{"email":"<INTERNHUB_BOOTSTRAP_ADMIN_EMAIL>","password":"<INTERNHUB_BOOTSTRAP_ADMIN_PASSWORD>"}` | 200, jeton JWT et profil Admin |
| POST | /auth/login | email/mot de passe incorrects | 401 |

Utilisez le jeton étudiant pour les consultations et candidatures; utilisez le jeton Admin pour les opérations d'administration ci-dessous.
Un étudiant ne peut pas attribuer son propre rôle, créer des entreprises/offres, lire les candidatures d'un autre étudiant, ni changer leur statut (403).

| N° | Méthode | URL | Corps (body JSON) | Résultat attendu |
|----|---------|-----|-------------------|------------------|
| 1 | GET | /roles | | 200, liste avec ADMIN et ETUDIANT |
| 2 | POST | /users | {"email":"second-admin@internhub.tn","password":"motdepasse-securise","nom":"Admin","prenom":"Second","tel":"20000000","roles":[{"id":1}]} | 200, utilisateur sans mot de passe |
| 3 | POST | /users | {"email":"nada@ensi.tn","password":"motdepasse-securise","nom":"Bouhachem","prenom":"Nada","tel":"21413874"} | 200, rôle ETUDIANT par défaut |
| 4 | GET | /users | | 200, 2 utilisateurs |
| 5 | POST | /entreprises | {"nom":"SofiaTech","secteur":"Informatique","email":"rh@sofiatech.tn","tel":"71000000","adresse":"Tunis"} | 200, id 1 |
| 6 | GET | /entreprises | | 200 |
| 7 | PUT | /entreprises/1 | {"nom":"SofiaTech SA","secteur":"Informatique","email":"rh@sofiatech.tn","tel":"71000000","adresse":"Lac 2, Tunis"} | 200, valeurs modifiées |
| 8 | POST | /offres/1 | {"titre":"Stage PFE Spring Boot","description":"Développement d'une API REST","domaine":"Génie logiciel","duree":6,"dateLimite":"2027-06-30"} | 200, offre liée à l'entreprise 1 |
| 9 | GET | /offres | | 200 |
| 10 | PUT | /offres/1 | {"titre":"Stage PFE Spring Boot et Angular","description":"API REST et interface web","domaine":"Génie logiciel","duree":6,"dateLimite":"2027-06-30"} | 200 |
| 11 | POST | /candidatures/2/1 | `form-data`: `etablissement=ENSI`, `niveauEtudes=3ème année ingénieur`, `localisation=Tunis, Tunisie`, `motivation=Je souhaite rejoindre cette entreprise.`, `cv` (type File, PDF) | 200, statut EN_ATTENTE avec les informations du candidat |
| 12 | POST | /candidatures/2/1 | (aucun) | 400, « Vous avez déjà candidaté à cette offre » |
| 13 | GET | /candidatures/user/2 | | 200, candidatures de l'étudiant 2 |
| 13a | GET | /candidatures/1/cv | (aucun) | 200 PDF pour l'administrateur ou l'étudiant propriétaire; 403 pour tout autre étudiant |
| 14 | PUT | /candidatures/1/statut?statut=ACCEPTEE | (aucun) | 200, statut ACCEPTEE |
| 15 | DELETE | /candidatures/1 | | 200 |
| 16 | POST | /users | {"email":"pas-un-email","nom":"X"} | 400, messages de validation |
| 17 | GET | /entreprises/999 | | 404, « Entreprise 999 introuvable » |
| 18 | POST | /offres/1 | {"titre":"Offre expirée","duree":3,"dateLimite":"2020-01-01"} puis POST /candidatures/2/<id de cette offre> | 400, « La date limite de cette offre est dépassée » |
| 19 | DELETE | /offres/1 | | 200 (ses candidatures sont supprimées avec) |
| 20 | DELETE | /entreprises/1 | | 200 (ses offres sont supprimées avec) |
