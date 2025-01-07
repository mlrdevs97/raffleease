import { ImageFile } from "./image-file";

export interface Image {
    id: number; 
    imageFile: ImageFile; 
    originalName: string; 
    filePath: string; 
}