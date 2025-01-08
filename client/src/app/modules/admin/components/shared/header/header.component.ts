import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AccessTokenService } from '../../../../../core/services/token/access-token.service';
import { AuthService } from '../../../../../core/services/auth/auth.service';
import { TokenRefreshScheduler } from '../../../../../core/services/token/token-refresh-scheduler.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  constructor(
    private tokenService: AccessTokenService,
    private authService: AuthService,
    private refreshScheduler: TokenRefreshScheduler,
    private router: Router
  ) {}

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        this.tokenService.clearToken();
        this.refreshScheduler.stopTokenRefreshSchedule();
        this.router.navigate(['/admin/auth']);
      }
    })
  }
}
