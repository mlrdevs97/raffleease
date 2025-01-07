import { Component, Input } from '@angular/core';
import { StatusCircleComponent } from '../../../../../../components/shared/status-circle/status-circle.component';
import { RaffleStatus } from '../../../../../../core/models/raffles/raffle';

@Component({
  selector: 'app-raffle-heading',
  standalone: true,
  imports: [],
  templateUrl: './raffle-heading.component.html',
  styleUrl: './raffle-heading.component.css'
})
export class RaffleHeadingComponent {
  @Input() title!: string; 
  @Input() price!: number;
  @Input() status!: RaffleStatus; 
}
