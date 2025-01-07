import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { OrderRequest } from '../../models/orders/order-request';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SuccessResponse } from '../../models/responses/success-response';

@Injectable({
  providedIn: 'root'
})
export class OrdersService {

  constructor(
    private httpClient: HttpClient
  ) { }

  private baseURL: string = `${environment.serverPath}/api/v1/orders`;

  purchase(): Observable<SuccessResponse<string>> {
    return this.httpClient.post(this.baseURL, {}) as Observable<SuccessResponse<string>>;
  }   
}