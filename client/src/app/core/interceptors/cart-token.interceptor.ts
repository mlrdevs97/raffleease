import { inject } from '@angular/core';
import { HttpRequest, HttpEvent, HttpInterceptorFn, HttpHandlerFn } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CartTokenService } from '../services/token/cart-token.service';

export const cartTokenInterceptor: HttpInterceptorFn = (
    req: HttpRequest<any>,
    next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
    const tokenService = inject(CartTokenService);
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
            Authorization: `X-Cart-Token ${token}`
        },
    });

    return next(cloned);
};
