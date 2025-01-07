import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SuccessResponse } from '../../models/responses/success-response';

@Injectable({
  providedIn: 'root'
})
export class StripeService {

  constructor(
    private httpClient: HttpClient
  ) { }

  private baseURL: string = `${environment.serverPath}/api/v1/stripe`;

  getPublicKey(): Observable<SuccessResponse<string>> {
    return this.httpClient.get(`${this.baseURL}/keys/public`) as Observable<SuccessResponse<string>>;
  } 
}