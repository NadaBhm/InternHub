# InternHub API

Spring Boot REST API for internship offers, with JWT authentication and role-based access.

## Run locally

Requirements: Java 17+, Maven, and MySQL (the defaults use XAMPP on port 3306).
The database `internhubdb` is created automatically. Start with `mvn spring-boot:run`
from this directory, or run `InternhubApplication` from the IDE.

Set a stable JWT signing secret and bootstrap the first administrator before starting:

```powershell
$env:INTERNHUB_JWT_SECRET = "<a private random value of at least 32 characters>"
$env:INTERNHUB_BOOTSTRAP_ADMIN_EMAIL = "admin@example.com"
$env:INTERNHUB_BOOTSTRAP_ADMIN_PASSWORD = "<a strong password>"
mvn spring-boot:run
```

Keep the secret and administrator password outside source control. If no JWT secret
is configured, a temporary development key is generated at startup and existing
tokens stop working after restart. The bootstrap account is created only when the
configured email does not exist; its password is not reset on later restarts.

Optional registration email notifications require SMTP settings (`MAIL_HOST`,
`MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`) and `INTERNHUB_ADMIN_EMAIL`. Without
these settings, registration succeeds and the backend logs that the notification
was skipped.
For a deployed frontend, set `INTERNHUB_ALLOWED_ORIGINS` to its exact origin(s),
comma-separated; the local default is `http://localhost:4200`.

## Demo data

Fictional presentation data is enabled by default. On startup, the backend creates:

- 5 demo companies
- 8 demo internship offers
- 4 demo student accounts
- 6 demo applications with different statuses

Demo student passwords are `StudentDemo123`. Their email addresses end with
`@demo.tn`. Set `INTERNHUB_DEMO_DATA=false` when starting the backend to disable
new demo records. Existing records are not deleted when this option is disabled.

## Authentication and permissions

- `POST /auth/register` accepts `email`, `password` (at least 8 characters), `nom`,
  optional `prenom` and `tel`. It always creates an `ETUDIANT` account and returns
  `{ "token": "...", "user": { ... } }`.
- `POST /auth/login` accepts `email` and `password`, returning the same response.
- Send the returned token as `Authorization: Bearer <token>` for protected routes.
- Both roles can view companies and offers. Only `ADMIN` can manage companies and
  offers, browse/manage users, and manage all applications. `ETUDIANT` can view and
  update their own profile, create applications as themselves, and view only their
  applications. The server verifies ownership; a client-supplied user ID is not
  trusted. The Angular app exposes a profile editor for students and user/role
  management for admins.
- Registration never accepts a role. Only an authenticated administrator can
  assign `ADMIN` through the user-management API.
- Applications require school, study level, location, and a PDF CV attachment
  (maximum 5 MB). Files are stored outside the database in `uploads/` by default;
  set `INTERNHUB_UPLOAD_DIR` to choose another persistent directory. The CV
  download route is `GET /candidatures/{id}/cv` and is limited to administrators
  and the student who submitted that application. Include the upload directory in
  backups because it contains the uploaded CV files.

See [TESTS_POSTMAN.md](./TESTS_POSTMAN.md) for request examples and authorization checks.
