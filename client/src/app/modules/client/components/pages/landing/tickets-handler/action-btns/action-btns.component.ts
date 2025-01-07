import { Component } from '@angular/core';
import { CartBtnComponent } from './cart-btn/cart-btn.component';
import { PurchaseBtnComponent } from '../../../../shared/purchase-btn/purchase-btn.component';

@Component({
  selector: 'app-action-btns',
  standalone: true,
  imports: [PurchaseBtnComponent, CartBtnComponent],
  templateUrl: './action-btns.component.html',
  styleUrl: './action-btns.component.css'
})
export class ActionBtnsComponent {
  IsBtnDisabled!: boolean;
}
