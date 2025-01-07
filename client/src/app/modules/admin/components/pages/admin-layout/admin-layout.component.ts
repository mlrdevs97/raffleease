import { Component } from '@angular/core';
import { ActivatedRoute, Data, Router, RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../../shared/header/header.component';
import { FooterComponent } from '../../shared/footer/footer.component';
import { ShareRafflesService } from '../../../../../core/services/raffles/share-raffles.service';
import { Raffle } from '../../../../../core/models/raffles/raffle';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent {

  constructor(
    private route: ActivatedRoute,
    private shareRaffles: ShareRafflesService
  ) { }

  setRaffles() {
    this.route.data.subscribe({
      next: (data: Data) => {
        const raffles: Raffle[] = data['raffles'] ?? [];
        const rafflesMap: Map<number, Raffle> = new Map(raffles.map((raffle: Raffle) => [raffle.id, raffle]));
        this.shareRaffles.updateRaffles(rafflesMap);
      }
    })
  }

  ngOnInit() {
    this.setRaffles();
  }
}
