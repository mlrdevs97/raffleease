import { catchError, EMPTY, Observable, of, map } from 'rxjs';
import { ActivatedRouteSnapshot, ResolveFn, Router, RouterStateSnapshot } from "@angular/router";
import { inject } from "@angular/core";
import { Raffle } from "../models/raffles/raffle";
import { RafflesService } from '../services/raffles/raffles.service';
import { AccessTokenService } from "../services/token/access-token.service";
import { SuccessResponse } from '../models/responses/success-response';

export const AdminRafflesResolver: ResolveFn<SuccessResponse<Raffle[]> | null> = (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
): Observable<SuccessResponse<Raffle[]> | null> => {
    const tokenService = inject(AccessTokenService);
    const rafflesService: RafflesService = inject(RafflesService);
    const router: Router = inject(Router);
    const token = tokenService.getToken();
    if (!token) return of(null);
    return rafflesService.getAll().pipe(
        catchError(() => {
            router.navigate(['/error']);
            return EMPTY;
        })
    ); 
}