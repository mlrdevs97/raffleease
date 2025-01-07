import { Image } from "./images/image";

export interface Raffle {
    id: number;
    title: string;
    description: string;
    startDate: string; 
    endDate: string;
    status: RaffleStatus;
    images: Image[];
    ticketPrice: number;
    availableTickets: number;
    soldTickets: number;
    totalTickets: number;
    firstTicketNumber: number;
    revenue: number;
    associationId: number;
    url: string;
}

export type RaffleStatus = 'PENDING' | 'ACTIVE' | 'COMPLETED' | 'PAUSED';