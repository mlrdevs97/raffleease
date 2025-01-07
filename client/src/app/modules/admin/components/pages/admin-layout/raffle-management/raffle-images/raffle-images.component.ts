import { Component, Input, SimpleChanges } from '@angular/core';
import { Image } from '../../../../../../../core/models/raffles/images/image';
import { ImageFile } from '../../../../../../../core/models/raffles/images/image-file';
import { ThumbnailsComponent } from './thumbnails/thumbnails.component';

@Component({
  selector: 'app-raffle-images',
  standalone: true,
  imports: [ThumbnailsComponent],
  templateUrl: './raffle-images.component.html',
  styleUrl: './raffle-images.component.css'
})
export class RaffleImagesComponent {
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

