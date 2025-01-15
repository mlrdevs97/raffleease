import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, filter, take, tap } from 'rxjs/operators';
import { AccessTokenService } from './access-token.service';
import { environment } from '../../../../environments/environment';
import { SuccessResponse } from '../../models/responses/success-response';
import { AuthResponse } from '../../models/auth/auth-response';
import { ErrorResponse } from '../../models/responses/error-response';

@Injectable({
  providedIn: 'root',
})
export class RefreshTokenService {
  private refreshing = false;
  private refreshTokenSubject = new BehaviorSubject<SuccessResponse<AuthResponse> | null>(null);
  private baseURL = `${environment.serverPath}/api/v1/tokens`;

  constructor(
    private httpClient: HttpClient,
    private accessTokenService: AccessTokenService
  ) {}

  refreshAccessToken(): Observable<SuccessResponse<AuthResponse>> {
    if (this.refreshing) {
      return this.refreshTokenSubject.asObservable().pipe(
        filter((response): response is SuccessResponse<AuthResponse> => response !== null),
        take(1) 
      );
    }
    this.refreshing = true;
    
    return this.httpClient.post<SuccessResponse<AuthResponse>>(`${this.baseURL}/refresh`, {}, {
        withCredentials: true
      }).pipe(
        tap((response: SuccessResponse<AuthResponse>) => {
          const newAccessToken: string = response.data!.accessToken;
          this.accessTokenService.setToken(newAccessToken);
          this.refreshing = false;
          this.refreshTokenSubject.next(response);
        }),
        catchError((error: ErrorResponse) => {
          this.refreshing = false;
          this.refreshTokenSubject.next(null); 
          return throwError(() => error); 
        })
    );
  }
}
