import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RandomSelectorComponent } from "./random-selector/random-selector.component";
import { TicketsSearcherComponent } from "./tickets-searcher/tickets-searcher.component";
import { Raffle } from '../../../../../../../core/models/raffles/raffle';
import { NotificationComponent } from '../../../../../../../components/shared/notifications/notifications.component';
import { NotificationService } from '../../../../../../../core/services/notifications/notifications.service';

@Component({
  selector: 'app-tickets-selector',
  standalone: true,
  imports: [RandomSelectorComponent, TicketsSearcherComponent, NotificationComponent],
  templateUrl: './tickets-selector.component.html',
  styleUrl: './tickets-selector.component.css'
})
export class TicketsSelectorComponent {
  @Input() raffle!: Raffle;

  constructor(
    private notificationService: NotificationService
  ) {}

  notifyReservation() {
    this.notificationService.showMessage('Ticket añadido al carrito', 'success');
  }
}