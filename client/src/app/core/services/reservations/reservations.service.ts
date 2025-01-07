import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../../../../environments/environment";
import { GenerateRandomRequest } from "../../models/tickets/reservations/generate-random-request";
import { ReservationResponse } from "../../models/tickets/reservations/reservation-response";
import { Observable } from "rxjs";
import { SearchRequest } from "../../models/tickets/reservations/search-request";
import { ReservationRequest } from "../../models/tickets/reservations/reservation-request";
import { Ticket } from "../../models/tickets/ticket";
import { ApiResponse } from "../../models/responses/api-response";
import { SuccessResponse } from "../../models/responses/success-response";
import { Cart } from "../../models/cart/cart";

@Injectable({
    providedIn: 'root'
})
export class ReservationsService {

    constructor(
        private httpClient: HttpClient
    ) { }


    private baseURL: string = `${environment.serverPath}/api/v1/tickets/reservations`;

    generateRandom(body: GenerateRandomRequest): Observable<SuccessResponse<Cart>> {
        return this.httpClient.post(`${this.baseURL}/random`, body) as Observable<SuccessResponse<Cart>>;
    }

    reserve(body: ReservationRequest): Observable<SuccessResponse<Cart>> {
        return this.httpClient.post(`${this.baseURL}`, body) as Observable<SuccessResponse<Cart>>;
    }

    release(ticketIds: number[]): Observable<void> {
        return this.httpClient.put<void>(`${this.baseURL}`, {ticketIds});
    }
}