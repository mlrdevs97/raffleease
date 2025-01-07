import { Injectable } from '@angular/core';
import { OrderRequest } from '../../models/orders/order-request';
import { Reservation } from '../../models/orders/reservation';
import { Cart } from '../../models/cart/cart';
import { Ticket } from '../../models/tickets/ticket';

@Injectable({
  providedIn: 'root'
})
export class ShareCartService {
  private cart!: Cart;

  public getCart(): Cart {
    return this.cart;
  }

  public setCart(cart: Cart) {
    this.cart = cart
  }
}
