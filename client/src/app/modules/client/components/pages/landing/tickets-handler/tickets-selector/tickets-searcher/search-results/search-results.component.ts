import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { Ticket } from '../../../../../../../../../core/models/tickets/ticket';

@Component({
  selector: 'app-search-results',
  standalone: true,
  imports: [],
  templateUrl: './search-results.component.html',
  styleUrl: './search-results.component.css'
})
export class SearchResultsComponent {
  @Input() display!: boolean;
  @Input() tickets!: Ticket[];
  @Output() selectedTicket: EventEmitter<Ticket> = new EventEmitter<Ticket>();

  selectTicket(ticket: Ticket) {
    this.selectedTicket.emit(ticket);
  }
}
