import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { forkJoin, Observable } from 'rxjs';
import { Subscription } from 'rxjs';

import {
  Application,
  ApplicationStatus,
  Company,
  CompanyPayload,
  InternhubApiService,
  Offer,
  ApplicationPayload,
  OfferPayload,
  Role,
  User,
  UserPayload
} from './internhub-api.service';
import { AuthenticationService } from './core/services/auth.service';

type ActiveRole = 'ADMIN' | 'ETUDIANT' | null;
type ViewKey = 'overview' | 'offers' | 'applications' | 'companies' | 'users';
type EditorKey = 'offer' | 'company' | 'user' | 'profile' | 'application' | null;

interface OfferForm {
  titre: string;
  description: string;
  domaine: string;
  duree: number | null;
  dateLimite: string;
  entrepriseId: number | null;
}

interface ApplicationForm {
  offre: Offer | null;
  etablissement: string;
  niveauEtudes: string;
  localisation: string;
  cvFile: File | null;
  motivation: string;
}

interface CompanyForm {
  nom: string;
  secteur: string;
  email: string;
  tel: string;
  adresse: string;
}

interface UserForm {
  email: string;
  password: string;
  tel: string;
  nom: string;
  prenom: string;
  roleId: number | null;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  readonly statuses: ApplicationStatus[] = ['EN_ATTENTE', 'ACCEPTEE', 'REFUSEE'];

  activeRole: ActiveRole = null;
  authMode: 'login' | 'register' = 'login';
  authForm = { email: '', password: '', nom: '', prenom: '', tel: '' };
  activeView: ViewKey = 'overview';
  activeUser: User | null = null;
  selectedUserId: number | null = null;
  searchTerm = '';
  selectedCompanyId: number | null = null;
  selectedStatus = 'ALL';
  users: User[] = [];
  roles: Role[] = [];
  companies: Company[] = [];
  offers: Offer[] = [];
  applications: Application[] = [];
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';
  editor: EditorKey = null;
  editingId: number | null = null;

  offerForm: OfferForm = this.emptyOfferForm();
  companyForm: CompanyForm = this.emptyCompanyForm();
  userForm: UserForm = this.emptyUserForm();
  applicationForm: ApplicationForm = this.emptyApplicationForm();
  private authSubscription?: Subscription;

  constructor(
    private readonly api: InternhubApiService,
    private readonly authenticationService: AuthenticationService
  ) { }

  ngOnInit(): void {
    this.authSubscription = this.authenticationService.currentUser$.subscribe(user => {
      if (user) {
        this.startSession(user);
        this.refreshData();
      } else if (this.activeRole !== null) {
        this.leaveSpace();
      }
    });
  }

  ngOnDestroy(): void {
    this.authSubscription?.unsubscribe();
  }

  get isAdmin(): boolean {
    return this.activeRole === 'ADMIN';
  }

  get isStudent(): boolean {
    return this.activeRole === 'ETUDIANT';
  }

  get pageTitle(): string {
    const titles: Record<ViewKey, string> = {
      overview: 'Vue d’ensemble',
      offers: 'Offres de stage',
      applications: 'Candidatures',
      companies: 'Entreprises',
      users: 'Utilisateurs'
    };
    return titles[this.activeView];
  }

  get profileInitials(): string {
    return this.activeUser ? this.initials(this.activeUser) : this.isAdmin ? 'AD' : 'ET';
  }

  get filteredOffers(): Offer[] {
    const query = this.searchTerm.trim().toLocaleLowerCase();
    return this.offers.filter(offer =>
      (this.selectedCompanyId === null || offer.entreprise.id === this.selectedCompanyId) &&
      (!query || [
      offer.titre,
      offer.domaine,
      offer.entreprise.nom
    ].some(value => value?.toLocaleLowerCase().includes(query))));
  }

  get filteredCompanies(): Company[] {
    const query = this.searchTerm.trim().toLocaleLowerCase();
    return this.companies.filter(company => !query || [
      company.nom,
      company.secteur,
      company.email
    ].some(value => value?.toLocaleLowerCase().includes(query)));
  }

  get filteredUsers(): User[] {
    const query = this.searchTerm.trim().toLocaleLowerCase();
    return this.users.filter(user => !query || [
      user.nom,
      user.prenom,
      user.email
    ].some(value => value?.toLocaleLowerCase().includes(query)));
  }

  get filteredApplications(): Application[] {
    return this.applications.filter(application =>
      this.selectedStatus === 'ALL' || application.statut === this.selectedStatus
    );
  }

  get activeOffersCount(): number {
    return this.offers.filter(offer => this.isOfferOpen(offer)).length;
  }

  get pendingApplicationsCount(): number {
    return this.applications.filter(application => application.statut === 'EN_ATTENTE').length;
  }

  get acceptedApplicationsCount(): number {
    return this.applications.filter(application => application.statut === 'ACCEPTEE').length;
  }

  refreshData(): void {
    this.errorMessage = '';
    if (this.activeRole === null) {
      this.loading = false;
      this.users = [];
      this.companies = [];
      this.offers = [];
      this.applications = [];
      return;
    }

    this.loading = true;
    if (this.isAdmin) {
      forkJoin({
        users: this.api.getUsers(),
        roles: this.api.getRoles(),
        companies: this.api.getCompanies(),
        offers: this.api.getOffers()
      }).subscribe({
        next: result => {
          this.users = result.users;
          this.roles = result.roles;
          this.companies = result.companies;
          this.offers = result.offers;
          this.loading = false;
          this.loadApplications();
        },
        error: error => {
          this.loading = false;
          this.setError(error, 'Impossible de charger les données administrateur.');
        }
      });
      return;
    }

    this.users = [];
    forkJoin({
      companies: this.api.getCompanies(),
      offers: this.api.getOffers()
    }).subscribe({
      next: result => {
        this.companies = result.companies;
        this.offers = result.offers;
        if (this.selectedUserId === null) {
          this.activeUser = null;
          this.applications = [];
          this.loading = false;
          return;
        }
        this.api.getUser(this.selectedUserId).subscribe({
          next: user => {
            if (!user.roles?.some(role => role.libelle.toLocaleUpperCase() === 'ETUDIANT')) {
              this.clearStudentSession();
              this.loading = false;
              this.setNotice('Ce profil n’est pas un compte étudiant.');
              return;
            }
            this.activeUser = user;
            this.loading = false;
            this.loadApplications();
          },
          error: error => {
            this.clearStudentSession();
            this.loading = false;
            this.setError(error, 'Compte étudiant inaccessible. Vous pouvez créer un compte étudiant.');
          }
        });
      },
      error: error => {
        this.loading = false;
        this.setError(error, 'Impossible de charger les offres et les entreprises.');
      }
    });
  }

  submitAuth(): void {
    if (this.saving) {
      return;
    }
    this.clearMessages();
    if (this.authMode === 'login') {
      this.authenticationService.login(this.authForm.email.trim(), this.authForm.password).subscribe({
        next: () => this.clearMessages(),
        error: error => this.setError(error, 'Connexion impossible. Vérifiez votre email et votre mot de passe.')
      });
      return;
    }

    this.authenticationService.register({
      email: this.authForm.email.trim(),
      password: this.authForm.password,
      nom: this.authForm.nom.trim(),
      prenom: this.authForm.prenom.trim(),
      tel: this.authForm.tel.trim()
    }).subscribe({
      next: () => this.clearMessages(),
      error: error => this.setError(error, 'La création du compte a échoué.')
    });
  }

  setAuthMode(mode: 'login' | 'register'): void {
    this.authMode = mode;
    this.clearMessages();
    this.authForm = { email: '', password: '', nom: '', prenom: '', tel: '' };
  }

  leaveSpace(): void {
    if (this.authenticationService.currentUserValue) {
      this.authenticationService.logout();
      return;
    }
    this.activeRole = null;
    this.activeUser = null;
    this.selectedUserId = null;
    this.activeView = 'overview';
    this.users = [];
    this.roles = [];
    this.companies = [];
    this.offers = [];
    this.applications = [];
    this.authForm = { email: '', password: '', nom: '', prenom: '', tel: '' };
    this.clearMessages();
  }

  private startSession(user: User | null): void {
    if (!user) {
      this.activeRole = null;
      this.activeUser = null;
      this.selectedUserId = null;
      return;
    }
    const role = user.roles.some(item => item.libelle.toUpperCase() === 'ADMIN')
      ? 'ADMIN'
      : user.roles.some(item => item.libelle.toUpperCase() === 'ETUDIANT')
        ? 'ETUDIANT'
        : null;
    if (role === null) {
      this.authenticationService.logout();
      this.setError(null, 'Ce compte ne possède aucun rôle autorisé.');
      return;
    }
    this.activeRole = role;
    this.activeUser = user;
    this.selectedUserId = role === 'ETUDIANT' ? user.id : null;
    this.activeView = 'overview';
  }

  loadApplications(): void {
    if (this.activeRole === null || (this.isStudent && this.selectedUserId === null)) {
      this.applications = [];
      return;
    }
    let request: Observable<Application[]>;
    if (this.isAdmin) {
      request = this.api.getApplications();
    } else if (this.selectedUserId !== null) {
      request = this.api.getApplications(this.selectedUserId);
    } else {
      this.applications = [];
      return;
    }
    request.subscribe({
      next: applications => {
        this.applications = applications;
      },
      error: error => this.setError(error, 'Impossible de charger les candidatures.')
    });
  }

  changeView(view: ViewKey): void {
    if (view === 'users' && !this.isAdmin) {
      return;
    }
    if (view !== 'offers') {
      this.selectedCompanyId = null;
    }
    this.activeView = view;
    this.searchTerm = '';
    this.clearMessages();
  }

  openCompanyOffers(company: Company): void {
    this.selectedCompanyId = company.id;
    this.searchTerm = '';
    this.activeView = 'offers';
    this.clearMessages();
  }

  clearCompanyFilter(): void {
    this.selectedCompanyId = null;
  }

  get selectedCompany(): Company | null {
    return this.selectedCompanyId === null
      ? null
      : this.companies.find(company => company.id === this.selectedCompanyId) ?? null;
  }

  companyOffersCount(company: Company): number {
    return this.offers.filter(offer => offer.entreprise.id === company.id).length;
  }

  isOfferOpen(offer: Offer): boolean {
    return !offer.dateLimite || offer.dateLimite >= this.today();
  }

  statusLabel(status: ApplicationStatus): string {
    const labels: Record<ApplicationStatus, string> = {
      EN_ATTENTE: 'En attente',
      ACCEPTEE: 'Acceptée',
      REFUSEE: 'Refusée'
    };
    return labels[status];
  }

  accountType(user: User): string {
    return user.roles.some(role => role.libelle.toLocaleUpperCase() === 'ADMIN')
      ? 'Administrateur'
      : 'Étudiant';
  }

  initials(user: User): string {
    return `${user.prenom?.charAt(0) ?? ''}${user.nom.charAt(0)}`.toLocaleUpperCase();
  }

  openOfferEditor(offer?: Offer): void {
    if (!this.isAdmin) {
      return;
    }
    this.clearMessages();
    this.editor = 'offer';
    this.editingId = offer?.id ?? null;
    this.offerForm = offer ? {
      titre: offer.titre,
      description: offer.description ?? '',
      domaine: offer.domaine ?? '',
      duree: offer.duree,
      dateLimite: offer.dateLimite ?? '',
      entrepriseId: offer.entreprise.id
    } : this.emptyOfferForm();
  }

  openCompanyEditor(company?: Company): void {
    if (!this.isAdmin) {
      return;
    }
    this.clearMessages();
    this.editor = 'company';
    this.editingId = company?.id ?? null;
    this.companyForm = company ? {
      nom: company.nom,
      secteur: company.secteur ?? '',
      email: company.email ?? '',
      tel: company.tel ?? '',
      adresse: company.adresse ?? ''
    } : this.emptyCompanyForm();
  }

  openUserEditor(user?: User): void {
    if (!this.isAdmin) {
      return;
    }
    this.clearMessages();
    this.editor = 'user';
    this.editingId = user?.id ?? null;
    this.userForm = user ? {
      email: user.email,
      password: '',
      tel: user.tel ?? '',
      nom: user.nom,
      prenom: user.prenom ?? '',
      roleId: user.roles[0]?.id ?? null
    } : {
      ...this.emptyUserForm(),
      roleId: this.roles.find(role => role.libelle.toUpperCase() === 'ETUDIANT')?.id ?? null
    };
  }

  openProfileEditor(): void {
    if (!this.isStudent || !this.activeUser) {
      return;
    }
    this.clearMessages();
    this.editor = 'profile';
    this.editingId = this.activeUser.id;
    this.userForm = {
      email: this.activeUser.email,
      password: '',
      tel: this.activeUser.tel ?? '',
      nom: this.activeUser.nom,
      prenom: this.activeUser.prenom ?? '',
      roleId: null
    };
  }

  closeEditor(): void {
    this.editor = null;
    this.editingId = null;
  }

  saveEditor(): void {
    if (this.saving || this.editor === null) {
      return;
    }
    this.saving = true;
    this.clearMessages();
    if (this.editor === 'offer') {
      this.saveOffer();
    } else if (this.editor === 'company') {
      this.saveCompany();
    } else if (this.editor === 'application') {
      this.saveApplication();
    } else if (this.editor === 'user' || this.editor === 'profile') {
      this.saveUser();
    }
  }

  applyToOffer(offer: Offer): void {
    if (!this.isStudent) {
      return;
    }
    if (this.selectedUserId === null) return;
    this.clearMessages();
    this.editor = 'application';
    this.applicationForm = {
      offre: offer,
      etablissement: '',
      niveauEtudes: '',
      localisation: '',
      cvFile: null,
      motivation: ''
    };
  }

  onCvSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (!file) {
      this.applicationForm.cvFile = null;
      return;
    }
    if (!file.name.toLocaleLowerCase().endsWith('.pdf')
      || (file.type !== '' && file.type !== 'application/pdf')) {
      this.applicationForm.cvFile = null;
      input.value = '';
      this.setNotice('Veuillez choisir un fichier PDF.', true);
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.applicationForm.cvFile = null;
      input.value = '';
      this.setNotice('Le CV ne doit pas dépasser 5 Mo.', true);
      return;
    }
    this.applicationForm.cvFile = file;
    this.clearMessages();
  }

  saveApplication(): void {
    if (this.selectedUserId === null || this.applicationForm.offre === null || this.applicationForm.cvFile === null) {
      return;
    }
    const payload: ApplicationPayload = {
      etablissement: this.applicationForm.etablissement.trim(),
      niveauEtudes: this.applicationForm.niveauEtudes.trim(),
      localisation: this.applicationForm.localisation.trim(),
      motivation: this.applicationForm.motivation.trim()
    };
    this.saving = true;
    this.api.createApplication(this.selectedUserId, this.applicationForm.offre.id, payload, this.applicationForm.cvFile).subscribe({
      next: () => {
        this.saving = false;
        this.closeEditor();
        this.setNotice('Votre candidature a bien été envoyée.');
        this.loadApplications();
      },
      error: error => {
        this.saving = false;
        this.setError(error, 'La candidature n’a pas pu être envoyée.');
      }
    });
  }

  downloadCv(application: Application): void {
    this.api.downloadApplicationCv(application.id).subscribe({
      next: blob => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = application.cvFileName || 'cv.pdf';
        link.click();
        window.setTimeout(() => URL.revokeObjectURL(url), 1000);
      },
      error: error => this.setError(error, 'Le CV n’a pas pu être téléchargé.')
    });
  }

  changeApplicationStatus(application: Application, status: ApplicationStatus): void {
    if (!this.isAdmin || status === application.statut) {
      return;
    }
    this.clearMessages();
    this.api.updateApplicationStatus(application.id, status).subscribe({
      next: () => {
        this.setNotice('Le statut de la candidature a été mis à jour.');
        this.loadApplications();
      },
      error: error => {
        this.setError(error, 'Le statut n’a pas pu être modifié.');
        this.loadApplications();
      }
    });
  }

  deleteApplication(application: Application): void {
    if (!this.isAdmin || !window.confirm(`Supprimer la candidature de ${application.user.prenom ?? ''} ${application.user.nom} ?`)) {
      return;
    }
    this.api.deleteApplication(application.id).subscribe({
      next: () => {
        this.setNotice('La candidature a été supprimée.');
        this.loadApplications();
      },
      error: error => this.setError(error, 'La candidature n’a pas pu être supprimée.')
    });
  }

  deleteOffer(offer: Offer): void {
    if (!this.isAdmin || !window.confirm(`Supprimer « ${offer.titre} » ? Les candidatures liées seront également supprimées.`)) {
      return;
    }
    this.deleteResource(this.api.deleteOffer(offer.id), 'L’offre et ses candidatures liées ont été supprimées.');
  }

  deleteCompany(company: Company): void {
    if (!this.isAdmin || !window.confirm(`Supprimer « ${company.nom} » ? Ses offres et les candidatures liées seront supprimées.`)) {
      return;
    }
    this.deleteResource(this.api.deleteCompany(company.id), 'L’entreprise et ses offres liées ont été supprimées.');
  }

  deleteUser(user: User): void {
    if (!this.isAdmin || !window.confirm(`Supprimer le compte de ${user.prenom ?? ''} ${user.nom} ?`)) {
      return;
    }
    this.deleteResource(this.api.deleteUser(user.id), 'Le compte utilisateur a été supprimé.');
  }

  private saveOffer(): void {
    if (!this.isAdmin) {
      this.failSave(new Error('Action réservée à l’administrateur.'), 'Action réservée à l’administrateur.');
      return;
    }
    const payload: OfferPayload = {
      titre: this.offerForm.titre.trim(),
      description: this.offerForm.description.trim(),
      domaine: this.offerForm.domaine.trim(),
      duree: this.offerForm.duree,
      dateLimite: this.offerForm.dateLimite || null
    };
    const request = this.editingId === null
      ? this.offerForm.entrepriseId === null
        ? null
        : this.api.createOffer(this.offerForm.entrepriseId, payload)
      : this.api.updateOffer(this.editingId, payload);
    if (request === null) {
      this.saving = false;
      this.setNotice('Choisissez une entreprise pour cette offre.', true);
      return;
    }
    request.subscribe({
      next: () => this.finishSave('L’offre a été enregistrée.'),
      error: error => this.failSave(error, 'L’offre n’a pas pu être enregistrée.')
    });
  }

  private saveCompany(): void {
    if (!this.isAdmin) {
      this.failSave(new Error('Action réservée à l’administrateur.'), 'Action réservée à l’administrateur.');
      return;
    }
    const payload: CompanyPayload = {
      nom: this.companyForm.nom.trim(),
      secteur: this.companyForm.secteur.trim(),
      email: this.companyForm.email.trim(),
      tel: this.companyForm.tel.trim(),
      adresse: this.companyForm.adresse.trim()
    };
    const request = this.editingId === null
      ? this.api.createCompany(payload)
      : this.api.updateCompany(this.editingId, payload);
    request.subscribe({
      next: () => this.finishSave('L’entreprise a été enregistrée.'),
      error: error => this.failSave(error, 'L’entreprise n’a pas pu être enregistrée.')
    });
  }

  private saveUser(): void {
    const isProfile = this.editor === 'profile';
    if (!this.isAdmin && !isProfile) {
      this.failSave(new Error('Action réservée à l’administrateur.'), 'Action réservée à l’administrateur.');
      return;
    }
    const payload: UserPayload = {
      email: this.userForm.email.trim(),
      tel: this.userForm.tel.trim(),
      nom: this.userForm.nom.trim(),
      prenom: this.userForm.prenom.trim(),
      roles: this.isAdmin && this.userForm.roleId !== null
        ? [{ id: this.userForm.roleId }]
        : this.activeUser?.roles.map(role => ({ id: role.id })) ?? []
    };
    if (this.userForm.password.trim()) {
      payload.password = this.userForm.password;
    }

    if (this.editingId === null) {
      this.api.createUser(payload).subscribe({
        next: () => this.finishSave('Le compte a été créé.'),
        error: error => this.failSave(error, 'Le compte n’a pas pu être créé.')
      });
      return;
    }

    this.api.updateUser(this.editingId, payload).subscribe({
      next: () => {
        if (isProfile) {
          this.api.getUser(this.editingId!).subscribe({
            next: user => {
              this.activeUser = user;
              this.finishSave('Votre profil a été mis à jour.');
            },
            error: error => this.failSave(error, 'Le profil a été enregistré mais n’a pas pu être rechargé.')
          });
          return;
        }
        this.finishSave('Le compte a été mis à jour.');
      },
      error: error => this.failSave(error, 'Le compte n’a pas pu être mis à jour.')
    });
  }

  private finishSave(message: string): void {
    this.saving = false;
    this.closeEditor();
    this.setNotice(message);
    this.refreshData();
  }

  private failSave(error: unknown, fallback: string): void {
    this.saving = false;
    this.setError(error, fallback);
  }

  private deleteResource(request: Observable<void>, message: string): void {
    this.clearMessages();
    request.subscribe({
      next: () => {
        this.setNotice(message);
        this.refreshData();
      },
      error: error => this.setError(error, 'La suppression a échoué.')
    });
  }

  private clearStudentSession(): void {
    this.activeUser = null;
    this.selectedUserId = null;
    this.authenticationService.logout();
    this.activeRole = null;
  }

  private setError(error: unknown, fallback: string): void {
    let message = fallback;
    if (error instanceof HttpErrorResponse) {
      const body: unknown = error.error;
      if (typeof body === 'string' && body.trim()) {
        message = body;
      } else if (body !== null && typeof body === 'object' && 'message' in body) {
        const detail = (body as { message?: unknown }).message;
        if (typeof detail === 'string' && detail.trim()) {
          message = detail;
        } else if (detail !== null && typeof detail === 'object') {
          message = Object.values(detail).filter(value => typeof value === 'string').join(', ') || fallback;
        }
      } else if (error.status === 0) {
        message = 'API injoignable. Vérifiez que le backend tourne sur http://localhost:8080.';
      }
    }
    this.setNotice(message, true);
  }

  private setNotice(message: string, isError = false): void {
    this.errorMessage = isError ? message : '';
    this.successMessage = isError ? '' : message;
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private emptyOfferForm(): OfferForm {
    return { titre: '', description: '', domaine: '', duree: null, dateLimite: '', entrepriseId: null };
  }

  private emptyCompanyForm(): CompanyForm {
    return { nom: '', secteur: '', email: '', tel: '', adresse: '' };
  }

  private emptyUserForm(): UserForm {
    return { email: '', password: '', tel: '', nom: '', prenom: '', roleId: null };
  }

  private emptyApplicationForm(): ApplicationForm {
    return {
      offre: null,
      etablissement: '',
      niveauEtudes: '',
      localisation: '',
      cvFile: null,
      motivation: ''
    };
  }

  private today(): string {
    const now = new Date();
    const localDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return localDate.toISOString().slice(0, 10);
  }
}
