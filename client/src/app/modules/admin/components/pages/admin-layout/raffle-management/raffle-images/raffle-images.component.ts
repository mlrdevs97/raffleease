import { Component, Input, SimpleChanges } from '@angular/core';
import { Image } from '../../../../../../../core/models/images/image';
import { ImageFile } from '../../../../../../../core/models/images/image-file';
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
  mainURL!: string;
  
  onImageSelected(url: string): void {
    this.mainURL = url;
  }

  createImageUrl(imageFile: ImageFile): string {
    const blob = new Blob([imageFile.data], { type: imageFile.contentType });
    return URL.createObjectURL(blob);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['images']) {
      this.mainURL = this.images[0].url;
    }
  }
}

