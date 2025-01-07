import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthTokenService } from '../../../../../core/services/token/auth-token.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  constructor(
    private tokenService: AuthTokenService,
    private router: Router
  ) {}

  logout() {
    this.tokenService.logout();
    this.router.navigate(['/admin/auth'])
  }

}
