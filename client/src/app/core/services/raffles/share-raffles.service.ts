import { Injectable } from '@angular/core';
import { Raffle } from '../../models/raffles/raffle';
import { BehaviorSubject, filter, map, Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ShareRafflesService {
  constructor() { }

  private raffles!: Map<number, Raffle>;
  private rafflesSubject: BehaviorSubject<Map<number, Raffle>> = new BehaviorSubject<Map<number, Raffle>>(this.raffles);
  rafflesUpdates: Observable<Map<number, Raffle>> = this.rafflesSubject.asObservable().pipe(filter(value => !!value));  
  
  updateRaffles(raffles: Map<number, Raffle>) {
    this.raffles = raffles;
    this.rafflesSubject.next(raffles);
  }

  setRaffle(raffle: Raffle): void {
    this.raffles.set(raffle.id, raffle);
  }

  get(id: number): Raffle | undefined {
    return this.raffles.get(id);
  }

  delete(id: number) {
    this.raffles.delete(id);
  }

  isNull() {
    return !this.raffles;
  }
}


