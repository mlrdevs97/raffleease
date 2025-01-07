import { Component } from '@angular/core';
import { ActivatedRoute, Data, Router } from '@angular/router';
import { Raffle } from '../../../../../core/models/raffles/raffle';
import { RaffleDescriptionComponent } from "./raffle-description/raffle-description.component";
import { ClientImagesComponent } from './client-images/client-images.component';
import { CartComponent } from './cart/cart.component';
import { HeaderComponent } from '../../shared/header/header.component';
import { RaffleHeadingComponent } from './raffle-heading/raffle-heading.component';
import { TicketsHandlerComponent } from './tickets-handler/tickets-handler.component';
import { FooterComponent } from '../../../../admin/components/shared/footer/footer.component';
import { UpdateAvailabilityService } from '../../../../../core/services/raffles/update-availability.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [FooterComponent, ClientImagesComponent, RaffleHeadingComponent, RaffleDescriptionComponent, TicketsHandlerComponent, CartComponent, HeaderComponent],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent {
  raffle!: Raffle
   
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private updateAvailabilityService: UpdateAvailabilityService
  ) {}

  setRaffle() {
    this.route.data.subscribe({
      next: (data: Data) => {
        const raffle: Raffle = data['raffle'];
        if (!raffle) this.router.navigate(['/client/unknown']);
        this.raffle = raffle;
      },
      error: (error: any) => {
        this.router.navigate(['/client/unknown']);
      }
    });
  }

  updateAvailableTickets() {
    this.updateAvailabilityService.amount$.subscribe({
      next: (update: [number, -1 | 1] | null) => {
        if (update) {
          this.raffle.availableTickets = update[1] === 1 
            ? this.raffle.availableTickets + update[0] 
            : this.raffle.availableTickets - update[0];
        }
      }
    })
  }


  ngOnInit() {
   this.setRaffle();
   this.updateAvailableTickets();
  }
  
}