import { Component, SimpleChanges } from '@angular/core';
import { EditionFormComponent } from './edition-form/edition-form.component';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { RafflesService } from '../../../../../../core/services/raffles/raffles.service';
import { ShareRafflesService } from '../../../../../../core/services/raffles/share-raffles.service';
import { Raffle } from '../../../../../../core/models/raffles/raffle';
import { RaffleEdit } from '../../../../../../core/models/raffles/raffle-edit';
import { SuccessResponse } from '../../../../../../core/models/responses/success-response';

@Component({
  selector: 'app-raffle-edition',
  standalone: true,
  imports: [EditionFormComponent],
  templateUrl: './raffle-edition.component.html',
  styleUrl: './raffle-edition.component.css'
})
export class RaffleEditionComponent {
  validationErrors: Record<string, string> = {};
  serverError: string | null = null;
  raffleId!: number;
  raffle!: Raffle;

  constructor(
    private rafflesService: RafflesService,
    private shareRaffles: ShareRafflesService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  edit(editRaffle: Partial<RaffleEdit>) {
    this.rafflesService.edit(this.raffleId, editRaffle).subscribe({
      next: (response: SuccessResponse<Raffle>) => {
        const raffle: Raffle = response.data!;
        this.shareRaffles.setRaffle(raffle);
      },
      error: (err: any) => {
        console.log(err);
      }
    })
  }

  private getRaffleId(): void {
    this.route.paramMap.subscribe({
      next: (params: ParamMap) => {
        const id: string | null = params.get('id');
        if (id) {
          this.raffleId = this.parseId(id);
          this.raffle = this.shareRaffles.get(this.raffleId)!;
        } else {
          this.shareRaffles.setRaffle(this.raffle);
          this.router.navigate(['/admin']);
        }
      }
    });
  }

  private parseId(raffleId: string): number {
    const parsedId = Number.parseFloat(raffleId);
    return isNaN(parsedId) ? 0 : parsedId;
  }

  ngOnInit() {
    this.getRaffleId();
  }
}
