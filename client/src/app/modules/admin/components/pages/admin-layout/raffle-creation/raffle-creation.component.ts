import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CreationFormComponent } from './creation-form/creation-form.component';
import { RafflesService } from '../../../../../../core/services/raffles/raffles.service';
import { ShareRafflesService } from '../../../../../../core/services/raffles/share-raffles.service';
import { Raffle } from '../../../../../../core/models/raffles/raffle';
import { SuccessResponse } from '../../../../../../core/models/responses/success-response';
import { RaffleCreate } from '../../../../../../core/models/raffles/raffle-create';

@Component({
  selector: 'app-raffle-creation',
  standalone: true,
  imports: [CreationFormComponent],
  templateUrl: './raffle-creation.component.html',
  styleUrl: './raffle-creation.component.css'
})
export class RaffleCreationComponent {
  validationErrors: Record<string, string> = {};
  serverError: string | null = null;

  constructor(
    private rafflesService: RafflesService,
    private shareRaffles: ShareRafflesService,
    private router: Router
  ) { }

  createRaffle(request: RaffleCreate) {
    console.log(request);
    this.rafflesService.create(request).subscribe({
      next: (response: SuccessResponse<Raffle>) => {
        const raffle: Raffle = response.data!;
        this.shareRaffles.setRaffle(raffle);
        this.router.navigate([`/admin/management/${raffle.id}`]);
      },
      error: (err: any) => {
        console.log(err)
      }
    })
  }
}
