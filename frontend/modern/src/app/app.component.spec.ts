import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BehaviorSubject, of } from 'rxjs';

import { AppComponent } from './app.component';
import { InternhubApiService } from './internhub-api.service';
import { AuthenticationService } from './core/services/auth.service';

const student = {
  id: 12,
  email: 'student@example.test',
  nom: 'Student',
  prenom: 'Test',
  tel: null,
  roles: [{ id: 2, libelle: 'ETUDIANT' }]
};

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let currentUser: BehaviorSubject<typeof student | null>;

  beforeEach(async () => {
    currentUser = new BehaviorSubject<typeof student | null>(null);
    const api = {
      getUsers: () => of([]),
      getRoles: () => of([]),
      getCompanies: () => of([]),
      getOffers: () => of([]),
      getApplications: () => of([]),
      getApplicationsByUser: () => of([]),
      getUser: () => of(student)
    };
    const authentication = {
      currentUser$: currentUser.asObservable(),
      currentUserValue: null,
      token: null,
      hasRole: () => false,
      login: () => of({ token: '', user: student }),
      register: () => of({ token: '', user: student }),
      logout: () => currentUser.next(null)
    };

    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule],
      declarations: [AppComponent],
      providers: [
        { provide: InternhubApiService, useValue: api },
        { provide: AuthenticationService, useValue: authentication }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
  });

  it('shows sign-in instead of allowing visitors to choose a role', () => {
    expect(fixture.componentInstance).toBeTruthy();
    expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain('Connexion');
  });

  it('uses the role returned by authentication to show the student workspace', () => {
    currentUser.next(student);
    fixture.componentInstance.changeView('offers');
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain('Offres de stage');
    expect(fixture.nativeElement.querySelector('[aria-label="Utilisateurs"]')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Mode interface uniquement');
  });
});
