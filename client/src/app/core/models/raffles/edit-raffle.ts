export interface EditRaffle {
    title?: string, 
    description?: string,
    endDate?: Date,
    newImages?: File[],
    deleteImageIds?: number[],
    ticketPrice?: number,
    totalTickets?: number
}
