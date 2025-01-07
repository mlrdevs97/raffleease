import { Component, Input, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { Raffle } from '../../../../../../../core/models/raffles/raffle';
import { StatusCircleComponent } from '../../../../../../../components/shared/status-circle/status-circle.component';

@Component({
  selector: 'app-raffle-card',
  standalone: true,
  imports: [StatusCircleComponent],
  templateUrl: './raffle-card.component.html',
  styleUrl: './raffle-card.component.css'
})
export class RaffleCardComponent {

  constructor(
    private router: Router
  ) { }

  @Input() raffle!: Raffle;
  @Input() images!: string[];
  startDate!: string;
  endDate!: string;

  openRaffle(id: number) {
    this.router.navigate(['/admin/management/', id]);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes['raffle']) {
      this.startDate = this.raffle.startDate ? this.dateTimeToDateString(new Date(this.raffle.startDate)) : "Sin publicar";
      this.endDate = this.dateTimeToDateString(new Date(this.raffle.endDate));
    }
  }

  dateTimeToDateString(startDate: Date): string {
    return startDate.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'numeric',
      day: 'numeric',
      timeZone: 'Europe/Madrid'
    });
  }

}