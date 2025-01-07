import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { CartService } from '../../pages/landing/cart/cart.service';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-purchase-btn',
  standalone: true,
  imports: [NgClass],
  templateUrl: './purchase-btn.component.html',
  styleUrl: './purchase-btn.component.css'
})
export class PurchaseBtnComponent {
  disabled: boolean = true;

  constructor(
    private cartService: CartService,
    private router: Router
  ) {}

  checkIfDisabled() {
    this.cartService.isBtnDisabled$.subscribe({
      next: (isDisabled: boolean) => {
        this.disabled = isDisabled;
      }
    });
  }

  purchase() {
    if (!this.disabled) this.router.navigate(['/client/payment']);
  }
 
  ngOnInit() {
    this.checkIfDisabled();
  }
}
