import { Component } from '@angular/core';
import { LoginFormComponent } from './login-form/login-form.component';
import { Router, RouterLink } from '@angular/router';
import { AuthRequest } from '../../../../../../core/models/auth/login-request';
import { AuthService } from '../../../../../../core/services/auth/auth.service';
import { AccessTokenService, AuthTokenService } from '../../../../../../core/services/token/access-token.service';
import { SuccessResponse } from '../../../../../../core/models/responses/success-response';
import { AuthResponse } from '../../../../../../core/models/auth/auth-response';
import { TokenRefreshScheduler } from '../../../../../../core/services/token/token-refresh-scheduler.service';

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
    private tokenService: AccessTokenService,
    private refreshScheduler: TokenRefreshScheduler,
    private router: Router,
  ) { }

  login(authRequest: AuthRequest) {
    this.authService.authenticate(authRequest).subscribe({
      next: (response: SuccessResponse<AuthResponse>) => {
        this.tokenService.setToken(response.data!.accessToken);
        this.router.navigate(['/admin']);
      },
      error: (error: any) => {
        console.log(error)
      }
    })
  }
}
