# InternHub frontend

Angular 15 frontend for the InternHub Spring Boot REST API. The interface uses the modern dashboard template and provides separate student and administrator workspaces.

- Students can create a student account, browse internship offers and companies, and view/apply to their own offers.
- Administrators can manage offers, companies, student accounts, and application statuses.
- Role management is intentionally not exposed in the frontend.

The current backend has no authentication or authorization. The workspace switch and role-based page visibility are UI-only and do not protect API calls. Do not use this as a security boundary.

## Run locally

1. Start the backend and its MySQL database. The API listens on `http://localhost:8080`.
2. From this directory, install dependencies with `npm install`.
3. Run `npm start` and open `http://localhost:4200`.

The API base URL is configured in `src/environments/environment.ts` and `environment.prod.ts`. The backend currently has no authentication endpoints, so the frontend does not add a login flow.

## API routes used

- `/users`, `/entreprises`, and `/offres` for account and resource management. New accounts are assigned the backend’s default `ETUDIANT` role.
- `/candidatures` and `/candidatures/user/{userId}` for application lists.
- `/candidatures/{userId}/{offreId}` to apply, `/candidatures/{id}/statut?statut=…` to update status, and `/candidatures/{id}` to delete.
