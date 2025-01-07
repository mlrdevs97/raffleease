import { Component, Input, SimpleChanges } from '@angular/core';
import { OverlayComponent } from './overlay/overlay.component';
import { CartService } from './cart.service';
import { Ticket } from '../../../../../../core/models/tickets/ticket';
import { ShareTicketsService } from '../../../../../../core/services/tickets/share-tickets.service';
import { PurchaseBtnComponent } from '../../../shared/purchase-btn/purchase-btn.component';
import { NgClass } from '@angular/common';
import { ItemsListComponent } from './items-list/items-list.component';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [OverlayComponent, PurchaseBtnComponent, NgClass, ItemsListComponent],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent {
  @Input() ticketPrice!: number;
  tickets: Ticket[] = [];

  constructor(
    private shareTickets: ShareTicketsService,
    private cartService: CartService
  ) { }

  getInitialTickets() {
    this.shareTickets.tickets.subscribe({
      next: (tickets: Ticket[]) => {
        this.cartService.addTickets(tickets);
      }
    });
  }

  getTickets() {
    this.cartService.ticekts$.subscribe({
      next: (tickets: Ticket[]) => {
        this.tickets = tickets;
      }
    })
  }

  get total() {
    return this.cartService.total;
  }

  get isCartVisible() {
    return this.cartService.isCartVisible;
  }

  toggleCart() {
    this.cartService.toggleCart();
  }

  ngOnInit() {
    this.getInitialTickets();
    this.getTickets();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['ticketPrice']) {
      this.cartService.setTicketsPrice(changes['ticketsPrice'].currentValue);
    }
  }
}
