import { Ticket } from "../ticket";

export interface ReservationResponse {
    tickets: Ticket[];
    reservationFlag: string;
}
  