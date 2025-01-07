import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [],
  templateUrl: './payment-success.component.html',
  styleUrls: ['./payment-success.component.css']
})
export class PaymentSuccessComponent implements OnInit {
  raffleId: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.getRaffleId();
  }

  private getRaffleId(): void {
    this.route.paramMap.subscribe({
      next: (params: ParamMap) => {
        const raffleId = params.get('id');
        if (raffleId) this.raffleId = params.get('id');
        else this.router.navigate(['/']);
      }
    });
  }

  redirect(): void {
    if (this.raffleId) {
      this.router.navigate([`/client/raffle/${this.raffleId}`]);
    }
  }
}
