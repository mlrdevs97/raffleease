import {  Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ShareRafflesService } from '../../../../../../../core/services/raffles/share-raffles.service';
import { Raffle } from '../../../../../../../core/models/raffles/raffle';
import { StatusCircleComponent } from '../../../../../../../components/shared/status-circle/status-circle.component';

@Component({
  selector: 'app-raffle-details',
  standalone: true,
  imports: [StatusCircleComponent],
  templateUrl: './raffle-details.component.html',
  styleUrl: './raffle-details.component.css'
})
export class RaffleDetailsComponent {
  @Input() raffleId!: number;
  @Input() raffle!: Raffle;
  @Output() raffleChange: EventEmitter<Raffle> = new EventEmitter<Raffle>();
  showFullDescription: boolean = false;

  constructor(
    private router: Router,
    private shareRaffles: ShareRafflesService
  ) { }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['raffleId']) this.getRaffle();
  }

  openLanding() {
    window.location.href = this.raffle.url;
  }

  toggleDescription(): void {
    this.showFullDescription = !this.showFullDescription;
  }

  private getRaffle(): void {
    this.shareRaffles.rafflesUpdates.subscribe({
      next: (raffles: Map<number, Raffle>) => {
        this.setRaffle(raffles);
      }
    });
  }

  private setRaffle(raffles: Map<number, Raffle>) {
    const raffle: Raffle | undefined = raffles.get(this.raffleId);
    if (raffle) {
      this.raffle = raffle;
      this.raffleChange.emit(raffle);
    } else {
      this.redirectToAdmin();
    }
  }

  private redirectToAdmin() {
    this.router.navigate(['/admin']);
  }

  get startDate(): string {
    if (this.raffle.status === 'PENDING') {
      return 'Sin Publicar';
    }
    return this.formatDate(new Date(this.raffle.startDate));
  }

  get endDate(): string {
    return this.formatDate(new Date(this.raffle.endDate));
  }

  private formatDate(date: Date): string {
    const options: Intl.DateTimeFormatOptions = {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    };
    return date.toLocaleDateString('es-ES', options);
  }
}
