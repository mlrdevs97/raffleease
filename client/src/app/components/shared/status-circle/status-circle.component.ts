import { Component, Input  } from '@angular/core';
import { NgClass } from '@angular/common';
import { RaffleStatus } from '../../../core/models/raffles/raffle';

@Component({
  selector: 'app-status-circle',
  standalone: true,
  imports: [NgClass],
  templateUrl: './status-circle.component.html',
  styleUrl: './status-circle.component.css'
})
export class StatusCircleComponent {
  @Input() iconSize: number = 16;
  @Input() textSize: number = 18;
  @Input() status!: RaffleStatus;
  @Input() blackOutline: boolean = false;

  private statusTranslations: Record<RaffleStatus, string> = {
    PENDING: 'Pendiente',
    ACTIVE: 'Activo',
    PAUSED: 'En pausa',
    COMPLETED: 'Finalizado'
  };

  get translatedStatus(): string {
    return this.statusTranslations[this.status] || this.status;
  }
}
