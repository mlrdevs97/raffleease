export interface Image {
    id: number; 
    uuid: string;
    filePath: string; 
    fileName: string;
    contentType: string;
    url: string;
    imageOrder?: number;
}