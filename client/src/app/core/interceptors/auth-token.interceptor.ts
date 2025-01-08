import { inject } from '@angular/core';
import { HttpRequest, HttpEvent, HttpInterceptorFn, HttpHandlerFn } from '@angular/common/http';
import { catchError, Observable, switchMap, throwError } from 'rxjs';
import { AccessTokenService } from '../services/token/access-token.service';
import { RefreshTokenService } from '../services/token/refresh-token.service';
import { ErrorResponse } from '../models/responses/error-response';
import { SuccessResponse } from '../models/responses/success-response';
import { AuthResponse } from '../models/auth/auth-response';

export const authTokenInterceptor: HttpInterceptorFn = (
    req: HttpRequest<any>,
    next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
    const accessTokenService = inject(AccessTokenService);
    const refreshTokenService = inject(RefreshTokenService);

    const token = accessTokenService.getToken();

    const excludedUrls = [
        '/api/v1/auth/authenticate',
        '/api/v1/auth/register'
    ];

    if (!token || excludedUrls.some((url: string) => req.url.includes(url))) {
        return next(req);
    }

    const cloned: HttpRequest<any> = req.clone({
        setHeaders: {
            Authorization: `Bearer ${token}`
        },
    });
    
    return next(cloned);
};
