import { Injectable } from "@angular/core";
import { AccessTokenService } from "../services/token/access-token.service";
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from "@angular/router";
import { AuthService } from '../services/auth/auth.service';
import { catchError, finalize, map, Observable, of, switchMap, throwError } from "rxjs";
import { TokenRefreshScheduler } from "../services/token/token-refresh-scheduler.service";
import { RefreshTokenService } from "../services/token/refresh-token.service";
import { SuccessResponse } from "../models/responses/success-response";
import { AuthResponse } from "../models/auth/auth-response";

@Injectable({
  providedIn: 'root'
})
export class AuthGuard {
  private isRefreshing = false;

  constructor(
    private tokenService: AccessTokenService,
    private authService: AuthService,
    private refreshTokenService: RefreshTokenService,
    private refreshscheduler: TokenRefreshScheduler,
    private router: Router
  ) { }

  canActivate: CanActivateFn = (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | boolean => {
    const token: string | null = this.tokenService.getToken();
    if (!token) {
      this.redirectToAuth();
      return false;
    }

    return this.authService.validate().pipe(
      map(() => true),
      catchError((error: any) => {
        if (error.status === 401 && !this.isRefreshing) {
          this.isRefreshing = true;
          return this.refreshTokenService.refreshAccessToken().pipe(
            switchMap((response: SuccessResponse<AuthResponse>) => {
              this.tokenService.setToken(response.data!.accessToken);
              this.isRefreshing = false;
              return of(true);
            }),
            catchError(() => this.handleError())
          );
        }
        return this.handleError();
      }),
      finalize(() => {
        this.isRefreshing = false;
      })
    );
  }

  private redirectToAuth(): void {
    this.router.navigate(['/admin/auth']);
  }

  private handleError(): Observable<boolean> {
    this.refreshscheduler.stopTokenRefreshSchedule();
    this.tokenService.clearToken();
    this.redirectToAuth();
    return of(false);
  }
}
