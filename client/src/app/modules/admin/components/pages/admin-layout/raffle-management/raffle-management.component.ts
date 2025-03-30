import { Component } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { RaffleImagesComponent } from "./raffle-images/raffle-images.component";
import { RaffleDetailsComponent } from './raffle-details/raffle-details.component';
import { ActionsComponent } from "./actions/actions.component";
import { ModalsComponent } from './modals/modals.component';
import { RafflesService } from '../../../../../../core/services/raffles/raffles.service';
import { ShareRafflesService } from '../../../../../../core/services/raffles/share-raffles.service';
import { Raffle } from '../../../../../../core/models/raffles/raffle';
import { StatisticsComponent } from './statistics/statistics.component';
import { SuccessResponse } from '../../../../../../core/models/responses/success-response';

@Component({
  selector: 'app-raffle-management',
  standalone: true,
  imports: [RaffleImagesComponent, RaffleDetailsComponent, ActionsComponent, ModalsComponent, StatisticsComponent],
  templateUrl: './raffle-management.component.html',
  styleUrl: './raffle-management.component.css'
})
export class RaffleManagementComponent {
  raffle!: Raffle;
  images!: string[];
  raffleId!: number;
  displayModal: boolean = false;
  modalHeader!: string;
  modalBody!: string;
  selectedAction!: 'publish' | 'pause' | 'restart' | 'delete' | null;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private rafflesService: RafflesService,
    private shareRaffles: ShareRafflesService,
  ) { }

  handleAction(action: 'publish' | 'pause' | 'restart' | 'delete') {
    this.selectedAction = action;
    this.displayModal = true;
  }

  onActionConfirmed() {
    switch (this.selectedAction) {
      case 'publish':
        this.publish();
        break;
      case 'pause':
        this.pause();
        break;
      case 'restart':
        this.restart();
        break;
      case 'delete':
        this.delete();
        break;
    }
  }

  hiddeModal() {
    this.displayModal = false;
  }

  publish() {
    this.rafflesService.publish(this.raffleId).subscribe({
      next: (response: SuccessResponse<Raffle>) => {
        this.updateRaffle(response.data!);
      }
    })
  }

  pause() {
    this.rafflesService.pause(this.raffleId).subscribe({
      next: (response: SuccessResponse<Raffle>) => {
        this.updateRaffle(response.data!);
      }
    })
  }

  restart() {
    this.rafflesService.restart(this.raffleId).subscribe({
      next: (response: SuccessResponse<Raffle>) => {
        this.updateRaffle(response.data!);
      }
    })
  }

  delete() {
    this.rafflesService.delete(this.raffleId).subscribe({
      next: () => {
        this.shareRaffles.delete(this.raffleId);
        this.router.navigate(['/admin'])
      }
    })
  }

  private updateRaffle(raffle: Raffle) {
    this.shareRaffles.delete(raffle.id);
    this.shareRaffles.setRaffle(raffle);
    this.raffle = raffle;
    this.displayModal = false;
  }

  private getRaffleId(): void {
    this.route.paramMap.subscribe({
      next: (params: ParamMap) => {
        const id: string | null = params.get('id');
        if (id) {
          this.raffleId = this.parseId(id);
          const raffle: Raffle | undefined = this.shareRaffles.get(this.raffleId);
          if (!raffle) {
             this.router.navigate(['/admin']);
          } else {
            this.raffle = raffle;
            console.log(this.raffle);
          }
        } else {
          this.router.navigate(['/admin'])
        };
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
