import { Injectable } from '@angular/core';
import { BehaviorSubject, filter, Observable } from 'rxjs';
import { Ticket } from '../../models/tickets/ticket';

@Injectable({
  providedIn: 'root'
})
export class ShareTicketsService {
  private ticketsSource = new BehaviorSubject<Ticket[] | null>(null);
  public tickets: Observable<Ticket[]> = this.ticketsSource
    .asObservable()
    .pipe(
      filter((tickets): tickets is Ticket[] => tickets !== null)
    );

  public setTickets(tickets: Ticket[]) {
    this.ticketsSource.next(tickets);
  }
}