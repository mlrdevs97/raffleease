import { NgClass } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-raffle-description',
  standalone: true,
  imports: [NgClass],
  templateUrl: './raffle-description.component.html',
  styleUrl: './raffle-description.component.css'
})
export class RaffleDescriptionComponent {
  @Input() description: string = '';
  showFullDescription: boolean = false;

  toggleDescription(): void {
    this.showFullDescription = !this.showFullDescription;
  }
}
