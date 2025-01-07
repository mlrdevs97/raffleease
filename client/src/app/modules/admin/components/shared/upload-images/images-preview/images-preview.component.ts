import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-images-preview',
  standalone: true,
  imports: [],
  templateUrl: './images-preview.component.html',
  styleUrl: './images-preview.component.css'
})
export class ImagesPreviewComponent {
  @Input() files: { id: number | null; file: File, url: string }[] = [];
  @Output() delete: EventEmitter<number> = new EventEmitter<number>();
  @Output() moveUp: EventEmitter<number> = new EventEmitter<number>();
  @Output() moveDown: EventEmitter<number> = new EventEmitter<number>();

  onMoveUp(index: number) {
    this.moveUp.emit(index);
  }

  onMoveDown(index: number) {
    this.moveDown.emit(index);
  }

  onDelete(index: number) {
    this.delete.emit(index);
  }
}
