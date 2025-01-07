import { Injectable } from '@angular/core';
import { Ticket } from '../../../../../../core/models/tickets/ticket';
import { BehaviorSubject, Observable } from 'rxjs';
import { TicketsService } from '../../../../../../core/services/tickets/tickets.service';
import { UpdateAvailabilityService } from '../../../../../../core/services/raffles/update-availability.service';
import { ReservationsService } from '../../../../../../core/services/reservations/reservations.service';

@Injectable({
  providedIn: 'root',
})
export class CartService {
  isCartVisible = false;
  private isBtnDisabledSource = new BehaviorSubject<boolean>(true); 
  isBtnDisabled$: Observable<boolean> = this.isBtnDisabledSource.asObservable();
  private ticektsSource = new BehaviorSubject<Ticket[]>([]);
  ticekts$: Observable<Ticket[]> = this.ticektsSource.asObservable();
  private price!: number;

  constructor(
    private reservationsService: ReservationsService,
    private updateAvailability: UpdateAvailabilityService
  ) {}

  getTickets() {
    return this.ticektsSource.getValue();
  }

  setTicketsPrice(ticketsPrice: number) {
    this.price = ticketsPrice;
  }

  get ticketsPrice(): number {
    return this.price;
  }

  addTickets(newTickets: Ticket[]) {
    const updatedTickets: Ticket[] =  [...this.getTickets(), ...newTickets];
    this.ticektsSource.next(updatedTickets);
    this.updateCheckoutBtnState(); 
  }

  removeTicket(ticket: Ticket) {
    const updatedTickets: Ticket[] = this.getTickets().filter((t: Ticket) => t.id !== ticket.id);
    this.ticektsSource.next(updatedTickets);
    this.release([ticket]);
  }

  removeAllTickets() {
    this.release(this.getTickets());
    this.ticektsSource.next([]);
  }

  get total() {
    return this.getTickets().reduce((sum, ticket: Ticket) => sum + this.ticketsPrice, 0);
  }  

  toggleCart() {
    this.isCartVisible = !this.isCartVisible; 
  }

  updateCheckoutBtnState() {
    const isDisabled: boolean = !(this.getTickets().length > 0) || !(this.total > 0);
    this.isBtnDisabledSource.next(isDisabled);
  }

  release(tickets: Ticket[]) {
    const ticketIds: number[] = tickets.map(ticket => ticket.id);
    this.reservationsService.release(ticketIds).subscribe({
      next: () => {
        this.updateCheckoutBtnState();
        this.updateAvailability.availabilitySource.next([tickets.length, 1]);
      }
    })
  }
}
