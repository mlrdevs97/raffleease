import { Component } from '@angular/core';
import { RegisterFormComponent } from './register-form/register-form.component';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../../../core/services/auth/auth.service';
import { RegisterRequest } from '../../../../../../core/models/auth/register-request';
import { HttpErrorResponse } from '@angular/common/http';
import { Constants } from '../../../../../../core/utils/constants/constants/constants.component';
import { AuthTokenService } from '../../../../../../core/services/token/auth-token.service';
import { SuccessResponse } from '../../../../../../core/models/responses/success-response';

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
    private tokenService: AuthTokenService,
    private router: Router
  ) { }

  register(registerRequest: RegisterRequest) {
    this.authService.register(registerRequest).subscribe({
      next: (response: SuccessResponse<string>) => {
        const token: string = response.data!;
        this.tokenService.setToken(token);
        this.router.navigate(['/admin']);
      },
      error: (error: any) => {
        console.log(error);
      }
    })
  }
}
