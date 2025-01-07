import { NgClass } from '@angular/common';
import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-overlay',
  standalone: true, 
  imports: [NgClass],
  templateUrl: './overlay.component.html',
  styleUrls: ['./overlay.component.css'],
})
export class OverlayComponent {
  @Input() isVisible = false;
  @Output() click = new EventEmitter<void>();

  onOverlayClick() {
    this.click.emit();
  }
}
