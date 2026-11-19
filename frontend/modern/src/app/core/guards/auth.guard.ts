import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthenticationService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
    constructor(private router: Router, private authenticationService: AuthenticationService) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const currentUser = this.authenticationService.currentUserValue;
        if (currentUser && this.authenticationService.token) {
            const requiredRoles = route.data['roles'] as string[] | undefined;
            if (!requiredRoles || requiredRoles.some(role => this.authenticationService.hasRole(role))) {
                return true;
            }
            return this.router.createUrlTree(['/']);
        }
        return this.router.createUrlTree(['/auth/login'], { queryParams: { returnUrl: state.url } });
    }
}
