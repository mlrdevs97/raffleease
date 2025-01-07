import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TicketsSelectorComponent } from './tickets-selector/tickets-selector.component';
import { ActionBtnsComponent } from './action-btns/action-btns.component';
import { Raffle } from '../../../../../../core/models/raffles/raffle';

@Component({
  selector: 'app-tickets-handler',
  standalone: true,
  imports: [TicketsSelectorComponent, ActionBtnsComponent],
  templateUrl: './tickets-handler.component.html',
  styleUrl: './tickets-handler.component.css'
})
export class TicketsHandlerComponent {
  @Input() raffle!: Raffle;
}
