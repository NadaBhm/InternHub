import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { User } from '../../internhub-api.service';

export interface AuthResponse {
  token: string;
  user: User;
}

export interface RegistrationRequest {
  email: string;
  password: string;
  nom: string;
  prenom: string;
  tel: string;
}

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
  private readonly authUrl = `${environment.apiUrl}/auth`;
  private readonly currentUserSubject = new BehaviorSubject<User | null>(this.readStoredUser());
  readonly currentUser$: Observable<User | null> = this.currentUserSubject.asObservable();

  constructor(private readonly http: HttpClient) { }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  get token(): string | null {
    return localStorage.getItem('token');
  }

  hasRole(roleName: string): boolean {
    return this.currentUserValue?.roles.some(
      role => role.libelle.toUpperCase() === roleName.toUpperCase()
    ) ?? false;
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, { email, password }).pipe(
      tap(response => this.saveSession(response))
    );
  }

  register(request: RegistrationRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/register`, request).pipe(
      tap(response => this.saveSession(response))
    );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  private saveSession(response: AuthResponse): void {
    localStorage.setItem('currentUser', JSON.stringify(response.user));
    localStorage.setItem('token', response.token);
    this.currentUserSubject.next(response.user);
  }

  private readStoredUser(): User | null {
    const storedUser = localStorage.getItem('currentUser');
    if (!storedUser || !localStorage.getItem('token')) {
      localStorage.removeItem('currentUser');
      localStorage.removeItem('token');
      return null;
    }
    try {
      const user = JSON.parse(storedUser) as User;
      if (user && Array.isArray(user.roles)) {
        return user;
      }
    } catch {
      // Discard malformed state rather than treating it as an authenticated account.
    }
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    return null;
  }
}
