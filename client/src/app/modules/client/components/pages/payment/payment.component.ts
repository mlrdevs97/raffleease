import { Component, KeyValueDiffers, OnInit } from '@angular/core';
import { Stripe, loadStripe } from '@stripe/stripe-js';
import { catchError, switchMap, take } from 'rxjs/operators';
import { from, of } from 'rxjs';
import { StripeService } from '../../../../../core/services/stripe/stripe.service';
import { OrdersService } from '../../../../../core/services/orders/orders.service';
import { OrderRequest } from '../../../../../core/models/orders/order-request';
import { CheckoutComponent } from './checkout/checkout.component';
import { SuccessResponse } from '../../../../../core/models/responses/success-response';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CheckoutComponent],
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css'],
})
export class PaymentComponent implements OnInit {
  orderRequest!: OrderRequest;
  stripe!: Stripe | null;
  publicKey!: string;
  clientSecret!: string;

  constructor(
    private stripeService: StripeService,
    private ordersService: OrdersService
  ) {}

  ngOnInit(): void {
    this.initializePaymentFlow();
  }

  private initializePaymentFlow(): void {
    this.stripeService
      .getPublicKey()
      .pipe(
        take(1),
        switchMap((response: SuccessResponse<string>) => {
          this.publicKey = response.data!;
          return from(loadStripe(this.publicKey)).pipe(
            switchMap((stripe) => {
              if (!stripe) {
                throw new Error('Failed to initialize Stripe: ' + stripe);
              } 
              this.stripe = stripe;
              return this.ordersService.purchase();
            })
          );
          
        }),
        catchError((error: any) => {
          console.log(error);
          return of(null); 
        })
      ).subscribe({
        next: (response: SuccessResponse<string> | null) => {
          if (response) {
            this.clientSecret = response.data!;
          } else {
            console.error('Failed to retrieve client secret');
          }
        }
      });
  }
}
