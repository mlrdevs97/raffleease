import { catchError, EMPTY, Observable } from "rxjs";
import { ActivatedRouteSnapshot, ResolveFn, Router, RouterStateSnapshot } from "@angular/router";
import { inject } from "@angular/core";
import { Raffle } from "../models/raffles/raffle";
import { RafflesService } from '../services/raffles/raffles.service';
import { SuccessResponse } from "../models/responses/success-response";

export const ClientRafflesResolver: ResolveFn<Observable<SuccessResponse<Raffle>>> = (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
): Observable<SuccessResponse<Raffle>> => {
    const rafflesService: RafflesService = inject(RafflesService);
    const router: Router = inject(Router);
    const id = parseId(route.url[route.url.length - 1]?.path ?? '');
    return rafflesService.get(id).pipe(
        catchError((error: any) => {
            router.navigate(['/error']);
            return EMPTY;
        })
    ); 
};

function parseId(raffleId: string): number {
    const parsedId = Number.parseInt(raffleId);
    return isNaN(parsedId) ? 0 : parsedId;
}
