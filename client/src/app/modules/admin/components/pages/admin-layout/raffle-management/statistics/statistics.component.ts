import { Component, Input } from '@angular/core';
import { Raffle } from '../../../../../../../core/models/raffles/raffle';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [],
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.css'
})
export class StatisticsComponent {
  @Input() raffle!: Raffle;

  get soldTickets(): number {
    return this.raffle.soldTickets ?? 0;
  } 

  get revenue(): number {
    return this.raffle.revenue ?? 0;
  } 

  get lastTicket(): number {
    return this.raffle.firstTicketNumber + this.raffle.totalTickets - 1;
  }

  get reserved(): number {
    return this.raffle.totalTickets - this.raffle.availableTickets;
  }
}
