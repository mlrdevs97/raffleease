import { inject } from '@angular/core';
import { HttpRequest, HttpEvent, HttpInterceptorFn, HttpHandlerFn } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthTokenService } from '../services/token/auth-token.service';

export const authTokenInterceptor: HttpInterceptorFn = (
    req: HttpRequest<any>,
    next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
    const tokenService = inject(AuthTokenService);
    const token = tokenService.getToken();

    if (!token) return next(req);

    const excludedUrls = [
        '/api/v1/auth/authenticate',
        '/api/v1/auth/register'
    ];

    if (excludedUrls.some(url => req.url.includes(url))) {
        return next(req);
    }

    const cloned = req.clone({
        setHeaders: {
            Authorization: `Bearer ${token}`
        },
    });

    return next(cloned);
};
