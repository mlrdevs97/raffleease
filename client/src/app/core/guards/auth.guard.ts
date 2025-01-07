import { Injectable } from "@angular/core";
import { AuthTokenService } from "../services/token/auth-token.service";
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from "@angular/router";
import { AuthService } from '../services/auth/auth.service';
import { catchError, map, Observable, of } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AuthGuard {
  constructor(
    private tokenService: AuthTokenService,
    private authService: AuthService,
    private router: Router
  ) { }

  canActivate: CanActivateFn = (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | boolean => {
    const token = this.tokenService.getToken();
    if (!token) {
      this.router.navigate(['/admin/auth']);
      return false;
    }
    return true;
    // return this.authService.validate().pipe(
    //   map(() => true), 
    //   catchError(() => {
    //     this.router.navigate(['/admin/auth']);
    //     return of(false);
    //   })
    // );
  }
}