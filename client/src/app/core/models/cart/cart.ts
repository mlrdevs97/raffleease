import { Ticket } from "../tickets/ticket";

export interface Cart {
    cartId: number;
    raffleId: number;
    tickets: Ticket[];
    status: CartStatus;
    lastModified: string;
    cartToken: string;
}

type CartStatus = 'ACTIVE' | 'EXPIRED' | 'CLOSED';