import { NgClass } from '@angular/common';
import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-quantity-controls',
  standalone: true,
  imports: [ReactiveFormsModule, NgClass],
  templateUrl: './quantity-controls.component.html',
  styleUrl: './quantity-controls.component.css'
})
export class QuantityControlsComponent {
  @Input() availableTickets!: number;
  @Input() reset!: boolean;
  @Output() quantityChange: EventEmitter<number> = new EventEmitter<number>();
  @Output() invalidChange: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() resetChange: EventEmitter<boolean> = new EventEmitter<boolean>();
  quantity: number = 0;
  quantityControl: FormControl = new FormControl();
  invalidInput: boolean = false;

  sendQuantity() {
    this.quantityControl.valueChanges.subscribe({
      next: (input: string) => {
        if (!this.isInvalidInput(input)) {
          const quantity: number = parseInt(input);
          this.quantity = ((quantity > this.availableTickets) ? this.availableTickets : ((quantity < 1) ? 1 : quantity));
          this.invalidInput = false;
          this.invalidChange.emit(false);
          this.quantityChange.emit(quantity);
        } else {
          this.invalidInput = true;
          this.invalidChange.emit(true);
        }
      }
    })
  }

  isInvalidInput(input: string): boolean {
    if (typeof input === 'string') {
      return !input ||
        input.length < 1 ||
        !input.trim() ||
        !/^\d*$/.test(input)
    }
    return false;
  }

  increase(): void {
    if (this.quantity < this.availableTickets) {
      this.quantity++;
      this.quantityControl.setValue(this.quantity);
    }
  }

  decrease(): void {
    if (this.quantity > 1) {
      this.quantity--;
      this.quantityControl.setValue(this.quantity);
    }
  }

  resetInput() {
    this.quantityControl.setValue("", { emitEvent: false });
    this.quantity = 0;
    this.invalidInput = true;
    this.resetChange.emit(false);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['reset']) {
      this.resetInput();
    }
  }

  ngOnInit() {
    this.sendQuantity();
  }
}