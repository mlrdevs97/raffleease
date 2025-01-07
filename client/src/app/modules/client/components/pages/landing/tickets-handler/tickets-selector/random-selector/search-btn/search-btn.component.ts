import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ShareTicketsService } from '../../../../../../../../../core/services/tickets/share-tickets.service';
import { UpdateAvailabilityService } from '../../../../../../../../../core/services/raffles/update-availability.service';
import { ShareCartService } from '../../../../../../../../../core/services/cart/share-cart.service';
import { SuccessResponse } from '../../../../../../../../../core/models/responses/success-response';
import { Cart } from '../../../../../../../../../core/models/cart/cart';
import { ReservationsService } from '../../../../../../../../../core/services/reservations/reservations.service';

@Component({
  selector: 'app-search-btn',
  standalone: true,
  imports: [],
  templateUrl: './search-btn.component.html',
  styleUrl: './search-btn.component.css'
})
export class SearchBtnComponent {
  @Input() quantity: number = 0;
  @Input() raffleId!: number;
  @Input() invalid!: boolean;
  @Output() ticketsReserved: EventEmitter<void> = new EventEmitter<void>();

  constructor(
    private reservationsService: ReservationsService,
    private shareCartService: ShareCartService,
    private shareTickets: ShareTicketsService,
    private updateAvailability: UpdateAvailabilityService
  ) { }

  reserve() {
    this.reservationsService.generateRandom({
      raffleId: this.raffleId,
      quantity: this.quantity
    }).subscribe({
      next: (response: SuccessResponse<Cart>) => {
        const cart: Cart = response.data!;
        this.shareCartService.setCart(cart);
        this.shareTickets.setTickets(cart.tickets);
        this.ticketsReserved.emit();
        this.updateAvailability.availabilitySource.next([cart.tickets.length, -1]);
      }
    })
  }
}
