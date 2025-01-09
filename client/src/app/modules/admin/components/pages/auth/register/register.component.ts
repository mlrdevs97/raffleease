import { Component } from '@angular/core';
import { RegisterFormComponent } from './register-form/register-form.component';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../../../core/services/auth/auth.service';
import { RegisterRequest } from '../../../../../../core/models/auth/register-request';
import { AccessTokenService } from '../../../../../../core/services/token/access-token.service';
import { SuccessResponse } from '../../../../../../core/models/responses/success-response';
import { AuthResponse } from '../../../../../../core/models/auth/auth-response';
import { TokenRefreshScheduler } from '../../../../../../core/services/token/token-refresh-scheduler.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [RegisterFormComponent, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  validationErrors: Record<string, string> = {};
  serverError: string | null = null;

  constructor(
    private authService: AuthService,
    private tokenService: AccessTokenService,
    private router: Router
  ) { }

  register(registerRequest: RegisterRequest) {
    this.authService.register(registerRequest).subscribe({
      next: (response: SuccessResponse<AuthResponse>) => {
        this.tokenService.setToken(response.data!.accessToken);
        this.router.navigate(['/admin']);
      },
      error: (error: any) => {
        console.log(error);
      }
    })
  }
}
