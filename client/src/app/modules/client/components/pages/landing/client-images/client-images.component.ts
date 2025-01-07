import { Component, Input, SimpleChanges } from '@angular/core';
import { ThumbnailsComponent } from "./thumbnails/thumbnails.component";
import { S3Service } from '../../../../../../core/services/images/images-service.service';
import { Image } from '../../../../../../core/models/raffles/images/image';
import { ImageFile } from '../../../../../../core/models/raffles/images/image-file';

@Component({
  selector: 'app-client-images',
  standalone: true,
  imports: [ThumbnailsComponent],
  templateUrl: './client-images.component.html',
  styleUrl: './client-images.component.css'
})
export class ClientImagesComponent {
  @Input() images!: Image[];
  urls!: string[];
  mainImage!: string;
  
  onImageSelected(image: string): void {
    this.mainImage = image;
  }

  convertImagesToUrls(images: Image[]): void {
    this.urls = images.map(image => this.createImageUrl(image.imageFile));
    this.mainImage = this.urls[0];
  }

  createImageUrl(imageFile: ImageFile): string {
    const blob = new Blob([imageFile.data], { type: imageFile.contentType });
    return URL.createObjectURL(blob);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['images'] && this.images) {
      this.convertImagesToUrls(this.images);
    }
  }
}
