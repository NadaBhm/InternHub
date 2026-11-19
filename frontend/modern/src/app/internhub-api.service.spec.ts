import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { InternhubApiService } from './internhub-api.service';

describe('InternhubApiService', () => {
  let api: InternhubApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    api = TestBed.inject(InternhubApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads one account without requesting the user directory', () => {
    api.getUser(12).subscribe();
    http.expectOne({ method: 'GET', url: 'http://localhost:8080/users/12' }).flush({
      id: 12,
      email: 'student@example.test',
      nom: 'Student',
      prenom: 'Test',
      tel: null,
      roles: [{ id: 2, libelle: 'ETUDIANT' }]
    });
  });

  it('loads all applications or the selected user’s applications from the backend routes', () => {
    api.getApplications().subscribe();
    http.expectOne({ method: 'GET', url: 'http://localhost:8080/candidatures' }).flush([]);

    api.getApplications(12).subscribe();
    http.expectOne({ method: 'GET', url: 'http://localhost:8080/candidatures/user/12' }).flush([]);
  });

  it('creates an application with candidate details and a PDF attachment', () => {
    const cv = new File(['%PDF-1.7'], 'cv-nada.pdf', { type: 'application/pdf' });
    api.createApplication(12, 4, {
      etablissement: 'ENSI',
      niveauEtudes: '3ème année',
      localisation: 'Tunis',
      motivation: 'Je souhaite rejoindre cette entreprise.'
    }, cv).subscribe();
    const request = http.expectOne({ method: 'POST', url: 'http://localhost:8080/candidatures/12/4' });
    expect(request.request.body instanceof FormData).toBeTrue();
    expect((request.request.body as FormData).get('etablissement')).toBe('ENSI');
    expect((request.request.body as FormData).get('cv')).toBe(cv);
    request.flush({});
  });

  it('updates application status using the statut query parameter', () => {
    api.updateApplicationStatus(9, 'ACCEPTEE').subscribe();
    const request = http.expectOne('http://localhost:8080/candidatures/9/statut?statut=ACCEPTEE');
    expect(request.request.method).toBe('PUT');
    request.flush({});
  });
});
