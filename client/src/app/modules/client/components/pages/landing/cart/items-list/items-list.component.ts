import { Component, Input } from '@angular/core';
import { Ticket } from '../../../../../../../core/models/tickets/ticket';
import { CartItemComponent } from './cart-item/cart-item.component';
import { CartService } from '../cart.service';

@Component({
  selector: 'app-items-list',
  standalone: true,
  imports: [CartItemComponent],
  templateUrl: './items-list.component.html',
  styleUrl: './items-list.component.css'
})
export class ItemsListComponent {
  @Input() tickets!: Ticket[];

  constructor(
    private cartService: CartService
  ) {}

  removeAll(): void {
    this.cartService.removeAllTickets();
  }

  get total() {
    return this.cartService.total;
  }
}
