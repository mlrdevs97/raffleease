export interface Ticket {
    id: number;
    ticketNumber: string;
    status: TicketStatus;
}

export type TicketStatus = 'AVAILABLE' | 'RESERVED' | 'SOLD';