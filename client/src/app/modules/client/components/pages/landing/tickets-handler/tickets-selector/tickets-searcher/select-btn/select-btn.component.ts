import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Ticket } from '../../../../../../../../../core/models/tickets/ticket';
import { ShareTicketsService } from '../../../../../../../../../core/services/tickets/share-tickets.service';
import { UpdateAvailabilityService } from '../../../../../../../../../core/services/raffles/update-availability.service';
import { ReservationsService } from '../../../../../../../../../core/services/reservations/reservations.service';
import { SuccessResponse } from '../../../../../../../../../core/models/responses/success-response';
import { Cart } from '../../../../../../../../../core/models/cart/cart';
import { ShareCartService } from '../../../../../../../../../core/services/cart/share-cart.service';
import { ErrorResponse } from '../../../../../../../../../core/models/responses/error-response';

@Component({
  selector: 'app-select-btn',
  standalone: true,
  imports: [],
  templateUrl: './select-btn.component.html',
  styleUrl: './select-btn.component.css'
})
export class SelectBtnComponent {
  @Input() raffleId!: number;
  @Input() ticket!: Ticket | null;
  @Input() invalid!: boolean;
  @Output() ticketReserved: EventEmitter<void> = new EventEmitter<void>();

  constructor(
    private reservationsService: ReservationsService,
    private shareCartService: ShareCartService,
    private shareTickets: ShareTicketsService,
    private updateAvailability: UpdateAvailabilityService
  ) { }

  reserve() {
    this.reservationsService.reserve({
      raffleId: this.raffleId,
      ticketsIds: [this.ticket!.id]
    }).subscribe({
      next: (response: SuccessResponse<Cart>) => {
        const cart: Cart = response.data!;
        this.shareCartService.setCart(cart);
        this.shareTickets.setTickets(cart.tickets);
        this.ticketReserved.emit();
        this.updateAvailability.availabilitySource.next([1, -1]);
      },
      error(error: ErrorResponse) {
        console.log(error);
      }
    })
  }
}
