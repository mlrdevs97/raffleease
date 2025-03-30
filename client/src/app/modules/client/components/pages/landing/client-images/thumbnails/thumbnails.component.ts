import { NgClass } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Image } from '../../../../../../../core/models/images/image';

@Component({
  selector: 'app-thumbnails',
  standalone: true,
  templateUrl: './thumbnails.component.html',
  styleUrl: './thumbnails.component.css'
})
export class ThumbnailsComponent {
  @Input() images!: Image[];
  @Input() activeImage!: string;
  @Output() imageSelected: EventEmitter<string> = new EventEmitter<string>();

  selectImage(image: string): void {
    this.imageSelected.emit(image);
  }
}
