import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Notification } from '../../models/notifications/notification';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private notificationSubject = new BehaviorSubject<Notification | null>(null);
  notification = this.notificationSubject.asObservable();

  showMessage(message: string, type: 'success' | 'error', duration: number = 3000): void {
    this.notificationSubject.next({ message, type });

    setTimeout(() => {
      this.notificationSubject.next(null);
    }, duration);
  }
}
