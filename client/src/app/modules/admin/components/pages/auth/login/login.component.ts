import { Component } from '@angular/core';
import { LoginFormComponent } from './login-form/login-form.component';
import { Router, RouterLink } from '@angular/router';
import { AuthRequest } from '../../../../../../core/models/auth/login-request';
import { AuthService } from '../../../../../../core/services/auth/auth.service';
import { AuthTokenService } from '../../../../../../core/services/token/auth-token.service';
import { SuccessResponse } from '../../../../../../core/models/responses/success-response';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [LoginFormComponent, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  validationErrors: Record<string, string> = {};
  serverError: string | null = null;

  constructor(
    private authService: AuthService,
    private tokenService: AuthTokenService,
    private router: Router,
  ) { }

  login(authRequest: AuthRequest) {
    this.authService.authenticate(authRequest).subscribe({
      next: (response: SuccessResponse<string>) => {
        const token: string = response.data!;
        this.tokenService.setToken(token);
        this.router.navigate(['/admin']);
      },
      error: (err: any) => {
        if (err.errors) {
          this.validationErrors = err.errors;
        } else {
          this.serverError = err.message;
        }
      }
    })
  }
}
