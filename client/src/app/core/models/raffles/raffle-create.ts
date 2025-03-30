import { Image } from '../images/image';
import { TicketsCreate } from '../tickets/raffle-tickets-creation-request';

export interface RaffleCreate {
    title: string, 
    description: string,
    endDate: Date,
    images: Image[],
    ticketsInfo: TicketsCreate
}
