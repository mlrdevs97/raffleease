import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";
import { Raffle } from "../models/raffles/raffle";

export const totalTicketsValidator = (raffle: Raffle): ValidatorFn => {
    return (control: AbstractControl): ValidationErrors | null => {
        if (!control.value) return null;

        const newQuantity = control.value;
        const minTickets: number = raffle.totalTickets;

        if (newQuantity < minTickets) {
            return { wrongQuantity: true };
        }

        return null;
    };
};
