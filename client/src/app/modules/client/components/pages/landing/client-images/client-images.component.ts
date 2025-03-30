import { Component, Input, SimpleChanges } from '@angular/core';
import { ThumbnailsComponent } from "./thumbnails/thumbnails.component";
import { Image } from '../../../../../../core/models/images/image';
import { ImageFile } from '../../../../../../core/models/images/image-file';

@Component({
  selector: 'app-client-images',
  standalone: true,
  imports: [ThumbnailsComponent],
  templateUrl: './client-images.component.html',
  styleUrl: './client-images.component.css'
})
export class ClientImagesComponent {
  @Input() images!: Image[];
  mainImage!: string;

  onImageSelected(image: string): void {
    this.mainImage = image;
  }

  createImageUrl(imageFile: ImageFile): string {
    const blob = new Blob([imageFile.data], { type: imageFile.contentType });
    return URL.createObjectURL(blob);
  }
}
