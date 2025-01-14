import { RaffleTicketsCreationRequest } from '../tickets/raffle-tickets-creation-request';

export interface RaffleCreationRequest {
    title: string, 
    description: string,
    endDate: Date,
    images: File[],
    ticketsInfo: RaffleTicketsCreationRequest
}
