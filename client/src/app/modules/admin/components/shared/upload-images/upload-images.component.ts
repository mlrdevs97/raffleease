import { Component, EventEmitter, Input, Output, SimpleChanges } from '@angular/core';
import { ImagesPreviewComponent } from "./images-preview/images-preview.component";
import { Image } from '../../../../../core/models/images/image';
import { ImagesService } from '../../../../../core/services/images/images-service.service';
import { SuccessResponse } from '../../../../../core/models/responses/success-response';

@Component({
  selector: 'app-upload-images',
  standalone: true,
  imports: [ImagesPreviewComponent],
  templateUrl: './upload-images.component.html',
  styleUrl: './upload-images.component.css'
})
export class UploadImagesComponent {
  @Input() images: Image[] = [];
  @Output() imagesChange = new EventEmitter<Image[]>();

  constructor(
    private imagesService: ImagesService
  ) { }

  onFilesSelected(event: any) {
    const files: File[] = Array.from(event.target.files);
    const formData: FormData = new FormData();
    files.forEach(file => {
      formData.append('files', file, file.name);
    });
    console.log(files);
    console.log(formData);
    this.imagesService.create(formData).subscribe({
      next: (response: SuccessResponse<Image[]>) => {
        const images: Image[] = response.data!;
        this.images.push(...images);
        console.log(this.images);
        this.setImagesOrder();
        this.imagesChange.emit(this.images);
      },
      error: (error: any) => {
        console.log(error);
      }
    });
  }

  onDelete(index: number) {
    const id: number = this.images[index].id;
    this.imagesService.delete(id).subscribe({
      next: () => {
        this.images.splice(index, 1);
        this.setImagesOrder();
        this.imagesChange.emit(this.images);    
      }
    })
  }

  swapItems(index: number, direction: number) {
    const newIndex = index + direction;
    if (newIndex >= 0 && newIndex < this.images.length) {
      [this.images[index], this.images[newIndex]] = [this.images[newIndex], this.images[index]];
    }
  }

  onMoveUp(index: number) {
    this.swapItems(index, -1);
    this.setImagesOrder();
    this.imagesChange.emit(this.images);
  }

  onMoveDown(index: number) {
    this.swapItems(index, 1);
    this.setImagesOrder();
    this.imagesChange.emit(this.images);
  }

  setImagesOrder() {
    this.images = this.images.map((image, index) => ({
      ...image,
      imageOrder: index + 1,
    }));
  }  
}
