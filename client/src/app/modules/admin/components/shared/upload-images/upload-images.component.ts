import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { ImagesPreviewComponent } from "./images-preview/images-preview.component";
import { Image } from '../../../../../core/models/raffles/images/image';

@Component({
  selector: 'app-upload-images',
  standalone: true,
  imports: [ImagesPreviewComponent],
  templateUrl: './upload-images.component.html',
  styleUrl: './upload-images.component.css'
})
export class UploadImagesComponent {
  @Input() images!: Image[];
  @Output() filesChange = new EventEmitter<{id: number | null; file: File, url: string}[]>();
  files: { id: number | null; file: File, url: string }[] = [];

  onFilesSelected(event: any) {
    const selectedFiles: File[] = Array.from(event.target.files);

    selectedFiles.forEach((file: File) => {
      this.files.push({
        id: null,
        file,
        url: URL.createObjectURL(file)
      })
    });

    this.filesChange.emit(this.files);
  }

  onDelete(index: number) {
    this.files.splice(index, 1);
    this.filesChange.emit(this.files);
  }

  swapItems(index: number, direction: number) {
    const newIndex = index + direction;
    if (newIndex >= 0 && newIndex < this.files.length) {
      [this.files[index], this.files[newIndex]] = [this.files[newIndex], this.files[index]];
    }
  }

  onMoveUp(index: number) {
    this.swapItems(index, -1);
    this.filesChange.emit(this.files);
  }

  onMoveDown(index: number) {
    this.swapItems(index, 1);
    this.filesChange.emit(this.files);
  }

  convertImagesToFiles(images: Image[]): void {
    this.files = images.map(image => {
      const { data, contentType } = image.imageFile;
      const { originalName } = image;
      const blob: Blob = new Blob([data], { type: contentType });
      const file: File = new File([blob], originalName, { type: contentType })
      return {
        id: image.id,
        file,
        url: URL.createObjectURL(file)
      };
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['images']) {
      const images: Image[] = changes['images'].currentValue;
      this.convertImagesToFiles(images);
    }
  }
}
