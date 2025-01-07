import { Component } from '@angular/core';
import { FloatingAddBtnComponent } from './floating-add-btn/floating-add-btn.component';
import { RaffleCardComponent } from './raffle-card/raffle-card.component';
import { ShareRafflesService } from '../../../../../../core/services/raffles/share-raffles.service';
import { Raffle } from '../../../../../../core/models/raffles/raffle';
import { StatusCircleComponent } from '../../../../../../components/shared/status-circle/status-circle.component';

@Component({
  selector: 'app-panel',
  standalone: true,
  imports: [FloatingAddBtnComponent, RaffleCardComponent, StatusCircleComponent],
  templateUrl: './panel.component.html',
  styleUrl: './panel.component.css'
})
export class PanelComponent {
  constructor(
    private shareRaffles: ShareRafflesService
  ) { }

  raffles!: Raffle[];
  images!: Map<number, string[]>;

  getRaffles() {
    if (this.shareRaffles.isNull()) {
      this.shareRaffles.rafflesUpdates.subscribe({
        next: (raffles: Map<number, Raffle>) => {
          this.raffles = Array.from(raffles.values());
        }
      });
    } else {
      this.raffles = Array.from(this.shareRaffles.getAll().values());
    }
  }

  ngOnInit() {
    this.getRaffles();
  }
}
