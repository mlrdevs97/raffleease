import { Injectable } from "@angular/core";
import { AccessTokenService } from "./access-token.service";
import { RefreshTokenService } from "./refresh-token.service";
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root',
})
export class TokenRefreshScheduler {
  private refreshTimeout: any;

  constructor(
    private refreshTokenService: RefreshTokenService,
    private accessTokenService: AccessTokenService
  ) {}

  startTokenRefreshSchedule() {
    const token: string | null = this.accessTokenService.getToken();
    if (!token) return;

    const expirationTime: number = this.getTokenExpirationTime(token);
    const refreshTime: number = expirationTime - Date.now() - 2 * 60 * 1000;

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
