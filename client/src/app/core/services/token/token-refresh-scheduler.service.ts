import { Injectable } from "@angular/core";
import { AccessTokenService } from "./access-token.service";
import { RefreshTokenService } from "./refresh-token.service";
import { jwtDecode } from 'jwt-decode';
import { Router } from "@angular/router";

@Injectable({
  providedIn: 'root',
})
export class TokenRefreshScheduler {
  private refreshTimeout: any;

  constructor(
    private refreshTokenService: RefreshTokenService,
    private accessTokenService: AccessTokenService,
    private router: Router
  ) {}

  startTokenRefreshSchedule() {
    const token: string | null = this.accessTokenService.getToken();
    if (!token) return;
    
    const expirationTime: number = this.getTokenExpirationTime(token) * 1000;
    const timeBeforeRefresh: number = 10000;
    const refreshDiff: number = expirationTime - timeBeforeRefresh;
    const now: number = Date.now();
    const refreshTime: number = refreshDiff > now ? refreshDiff - now : 3000;

    this.refreshTimeout = setTimeout(() => {
      this.refreshTokenService.refreshAccessToken().subscribe({
        next: () => this.startTokenRefreshSchedule(),
        error: () => this.stopTokenRefreshSchedule(),
      });
    }, refreshTime);
  }

  stopTokenRefreshSchedule() {
    clearTimeout(this.refreshTimeout);
  }

  private getTokenExpirationTime(token: string): number {
    try {
      const payload: { exp: number } = jwtDecode(token);
      return payload.exp;
    } catch (error: any) {
      console.error('Error decoding token:', error);
      throw new Error('Invalid JWT token');
    }
  }
}
