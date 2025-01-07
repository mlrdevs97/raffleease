import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Ticket } from '../../models/tickets/ticket';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { SuccessResponse } from '../../models/responses/success-response';

@Injectable({
  providedIn: 'root'
})
export class TicketsService {

  constructor(
    private httpClient: HttpClient
  ) { }


  private baseURL: string = `${environment.serverPath}/api/v1/tickets`;

  search(raffleId: number, ticketNumber: string): Observable<SuccessResponse<Ticket[]>> {
    const params = new HttpParams()
      .set('raffleId', raffleId)
      .set('ticketNumber', ticketNumber);

    return this.httpClient.get(this.baseURL, {params}) as Observable<SuccessResponse<Ticket[]>>;
  }
}