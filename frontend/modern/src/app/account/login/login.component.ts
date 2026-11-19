import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

// Login Auth
import { AuthenticationService } from '../../core/services/auth.service';
import { ToastService } from './toast-service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})

/**
 * Login Component
 */
export class LoginComponent implements OnInit {

  // Login Form
  loginForm!: UntypedFormGroup;
  submitted = false;
  fieldTextType!: boolean;
  error = '';
  returnUrl!: string;
  // set the current year
  year: number = new Date().getFullYear();

  constructor(private formBuilder: UntypedFormBuilder,private authenticationService: AuthenticationService,private router: Router,
    private route: ActivatedRoute,
    public toastservice: ToastService) {
      // redirect to home if already logged in
      if (this.authenticationService.currentUserValue) {
        this.router.navigate(['/']);
      }
     }

  ngOnInit(): void {
    if(localStorage.getItem('currentUser')) {
      this.router.navigate(['/']);
    }
    /**
     * Form Validatyion
     */
     this.loginForm = this.formBuilder.group({
     email: ['', [Validators.required, Validators.email]],
     password: ['', [Validators.required]],
    });
    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  // convenience getter for easy access to form fields
  get f() { return this.loginForm.controls; }

  /**
   * Form submit
   */
   onSubmit() {
    this.submitted = true;

     // Login Api
     if (this.loginForm.invalid) {
      return;
    }
    this.authenticationService.login(this.f['email'].value, this.f['password'].value).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: error => this.toastservice.show(
        error?.error?.message || 'Email ou mot de passe incorrect.',
        { classname: 'bg-danger text-white', delay: 15000 }
      )
    });
  }

  /**
   * Password Hide/Show
   */
   toggleFieldTextType() {
    this.fieldTextType = !this.fieldTextType;
  }

}
