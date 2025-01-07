import { Reservation } from "./reservation"

export interface OrderRequest {
    raffleId: number,
    reservations: Reservation[]
}