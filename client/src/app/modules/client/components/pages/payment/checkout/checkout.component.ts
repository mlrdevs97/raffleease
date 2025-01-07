import { Component, Input, SimpleChanges } from '@angular/core';
import { Stripe, StripeEmbeddedCheckout } from '@stripe/stripe-js';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent {
  @Input() stripe!: Stripe | null;
  @Input() clientSecret!: string;

  checkout: StripeEmbeddedCheckout | null = null;
  isProcessing = false;
  message: string | null = null;

  async mount() {
    if (this.stripe) {
      this.checkout = await this.stripe.initEmbeddedCheckout({ clientSecret: this.clientSecret });
      this.checkout.mount('#checkout');
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['clientSecret'] && this.clientSecret) {
      this.mount();
    }
  }

}