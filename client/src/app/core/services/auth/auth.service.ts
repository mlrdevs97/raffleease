import { Injectable } from "@angular/core";
import { HttpClient } from '@angular/common/http';
import { AuthRequest } from "../../models/auth/login-request";
import { Observable } from "rxjs";
import { RegisterRequest } from "../../models/auth/register-request";
import { environment } from "../../../../environments/environment";
import { SuccessResponse } from "../../models/responses/success-response";
import { AuthResponse } from "../../models/auth/auth-response";

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    constructor(
        private httpClient: HttpClient
    ) { }

    private baseURL: string = `${environment.serverPath}/api/v1/auth`;

    register(registerRequest: RegisterRequest): Observable<SuccessResponse<AuthResponse>> {
        return this.httpClient.post<SuccessResponse<AuthResponse>>(`${this.baseURL}/register`, registerRequest);
    }

    authenticate(authRequest: AuthRequest): Observable<SuccessResponse<AuthResponse>> {
        return this.httpClient.post<SuccessResponse<AuthResponse>>(`${this.baseURL}/authenticate`, authRequest);
    }

    logout(): Observable<void> {
        return this.httpClient.post<void>(`${this.baseURL}/logout`, {});
    }

    validate(): Observable<void> {
        return this.httpClient.get<void>(`${this.baseURL}/validate`);
    }
}  