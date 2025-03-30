import { Image } from "../images/image";

export interface RaffleEdit {
    title?: string, 
    description?: string,
    endDate?: Date,
    images: Image[],
    ticketPrice?: number,
    totalTickets?: number
}