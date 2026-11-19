import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../environments/environment';

export interface Role {
  id: number;
  libelle: string;
}

export interface User {
  id: number;
  email: string;
  tel: string | null;
  nom: string;
  prenom: string | null;
  roles: Role[];
}

export interface Company {
  id: number;
  nom: string;
  secteur: string | null;
  email: string | null;
  tel: string | null;
  adresse: string | null;
}

export interface Offer {
  id: number;
  titre: string;
  description: string | null;
  domaine: string | null;
  duree: number | null;
  dateLimite: string | null;
  entreprise: Company;
}

export type ApplicationStatus = 'EN_ATTENTE' | 'ACCEPTEE' | 'REFUSEE';

export interface Application {
  id: number;
  dateCandidature: string | null;
  statut: ApplicationStatus;
  user: User;
  offre: Offer;
  etablissement: string | null;
  niveauEtudes: string | null;
  localisation: string | null;
  cvLocation: string | null;
  cvFileName: string | null;
  motivation: string | null;
}

export interface ApplicationPayload {
  etablissement: string;
  niveauEtudes: string;
  localisation: string;
  motivation: string;
}

export interface UserPayload {
  email: string;
  password?: string;
  tel: string;
  nom: string;
  prenom: string;
  roles: Array<{ id: number }>;
}

export interface CompanyPayload {
  nom: string;
  secteur: string;
  email: string;
  tel: string;
  adresse: string;
}

export interface OfferPayload {
  titre: string;
  description: string;
  domaine: string;
  duree: number | null;
  dateLimite: string | null;
}

@Injectable({ providedIn: 'root' })
export class InternhubApiService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) { }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/users`);
  }

  getRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.baseUrl}/roles`);
  }

  getUser(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/users/${id}`);
  }

  createUser(payload: UserPayload): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users`, payload);
  }

  updateUser(id: number, payload: UserPayload): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/users/${id}`, payload);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/users/${id}`);
  }

  getCompanies(): Observable<Company[]> {
    return this.http.get<Company[]>(`${this.baseUrl}/entreprises`);
  }

  createCompany(payload: CompanyPayload): Observable<Company> {
    return this.http.post<Company>(`${this.baseUrl}/entreprises`, payload);
  }

  updateCompany(id: number, payload: CompanyPayload): Observable<Company> {
    return this.http.put<Company>(`${this.baseUrl}/entreprises/${id}`, payload);
  }

  deleteCompany(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/entreprises/${id}`);
  }

  getOffers(): Observable<Offer[]> {
    return this.http.get<Offer[]>(`${this.baseUrl}/offres`);
  }

  createOffer(companyId: number, payload: OfferPayload): Observable<Offer> {
    return this.http.post<Offer>(`${this.baseUrl}/offres/${companyId}`, payload);
  }

  updateOffer(id: number, payload: OfferPayload): Observable<Offer> {
    return this.http.put<Offer>(`${this.baseUrl}/offres/${id}`, payload);
  }

  deleteOffer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/offres/${id}`);
  }

  getApplications(userId?: number): Observable<Application[]> {
    const url = userId === undefined
      ? `${this.baseUrl}/candidatures`
      : `${this.baseUrl}/candidatures/user/${userId}`;
    return this.http.get<Application[]>(url);
  }

  createApplication(userId: number, offerId: number, payload: ApplicationPayload, cv: File): Observable<Application> {
    const formData = new FormData();
    formData.append('etablissement', payload.etablissement);
    formData.append('niveauEtudes', payload.niveauEtudes);
    formData.append('localisation', payload.localisation);
    formData.append('motivation', payload.motivation);
    formData.append('cv', cv, cv.name);
    return this.http.post<Application>(`${this.baseUrl}/candidatures/${userId}/${offerId}`, formData);
  }

  downloadApplicationCv(applicationId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/candidatures/${applicationId}/cv`, { responseType: 'blob' });
  }

  updateApplicationStatus(id: number, status: ApplicationStatus): Observable<Application> {
    const params = new HttpParams().set('statut', status);
    return this.http.put<Application>(`${this.baseUrl}/candidatures/${id}/statut`, null, { params });
  }

  deleteApplication(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/candidatures/${id}`);
  }
}
