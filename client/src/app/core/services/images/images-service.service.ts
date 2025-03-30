import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, switchMap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../models/responses/api-response';
import { SuccessResponse } from '../../models/responses/success-response';
import { Image } from '../../models/images/image';

@Injectable({
  providedIn: 'root'
})
export class ImagesService {

  constructor(
    private httpClient: HttpClient
  ) { }

  private baseURL: string = `${environment.serverPath}/api/v1/raffles/images`;

  create(files: FormData): Observable<SuccessResponse<Image[]>> {
    return this.httpClient.post<SuccessResponse<Image[]>>(this.baseURL, files)
  }

  delete(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.baseURL}/${id}`);
  }
}
