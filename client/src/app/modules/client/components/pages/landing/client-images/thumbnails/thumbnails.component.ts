import { NgClass } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-thumbnails',
  standalone: true,
  templateUrl: './thumbnails.component.html',
  styleUrl: './thumbnails.component.css'
})
export class ThumbnailsComponent {
  @Input() urls: string[] = [];
  @Input() activeImage: string = '';
  @Output() imageSelected: EventEmitter<string> = new EventEmitter<string>();

  selectImage(image: string): void {
    this.imageSelected.emit(image);
  }
}
