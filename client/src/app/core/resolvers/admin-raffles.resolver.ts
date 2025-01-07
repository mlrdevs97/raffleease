import { catchError, EMPTY, Observable, of, map } from 'rxjs';
import { ActivatedRouteSnapshot, ResolveFn, Router, RouterStateSnapshot } from "@angular/router";
import { inject } from "@angular/core";
import { Raffle } from "../models/raffles/raffle";
import { RafflesService } from '../services/raffles/raffles.service';
import { AuthTokenService } from "../services/token/auth-token.service";
import { SuccessResponse } from '../models/responses/success-response';

export const AdminRafflesResolver: ResolveFn<SuccessResponse<Raffle[]> | undefined> = (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
): Observable<SuccessResponse<Raffle[]> | undefined> => {
    const tokenService = inject(AuthTokenService);
    const rafflesService: RafflesService = inject(RafflesService);
    const router: Router = inject(Router);
    const token = tokenService.getToken();
    if (!token) return of(undefined);
    return rafflesService.getAll().pipe(
        catchError((error: any) => {
            router.navigate(['/error']);
            return EMPTY;
        })
    ); 
}