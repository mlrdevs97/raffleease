import { Injectable } from '@angular/core';
import { BehaviorSubject, filter, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UpdateAvailabilityService {

  constructor() { }

  availabilitySource: BehaviorSubject<[number, -1 | 1] | null> = new BehaviorSubject<[number, -1 | 1] | null>(null);
  amount$: Observable<[number, -1 | 1] | null> = this.availabilitySource.asObservable().pipe(
    filter(value => !!value)
  );
}
