import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthTokenService {

  constructor(
    private router: Router
  ) { }

  setToken(token: string): void {
    localStorage.setItem('auth-token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('auth-token');
  }

  logout() {
    localStorage.removeItem('auth-token');
    this.router.navigate(['/admin']);
  }
}