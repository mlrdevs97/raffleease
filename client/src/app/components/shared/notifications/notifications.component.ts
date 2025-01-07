import { Component } from '@angular/core';
import { NotificationService } from '../../../core/services/notifications/notifications.service';
import { Notification } from '../../../core/models/notifications/notification';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [NgClass],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css'],
})
export class NotificationComponent {
  notification: Notification | null = null;
  isVisible: boolean = false;

  constructor(
    private notificationService: NotificationService
  ) { }

  displayNotification() {
    this.notificationService.notification.subscribe((notification) => {
      this.notification = notification;
      if (notification) {
        this.isVisible = true;
        setTimeout(() => {
          this.isVisible = false;
        }, 5000);
      }
    });
  }

  ngOnInit() {
    this.displayNotification();
  }
}