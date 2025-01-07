import { Component } from '@angular/core';
import { CartService } from '../../../cart/cart.service';

@Component({
  selector: 'app-cart-btn',
  standalone: true,
  imports: [],
  templateUrl: './cart-btn.component.html',
  styleUrl: './cart-btn.component.css'
})
export class CartBtnComponent {
  constructor(
    private cartService: CartService
  ) {}

  get isCartVisible(): boolean {
    return this.cartService.isCartVisible;
  }

  toggleCart() {
    this.cartService.toggleCart();
  }
}
