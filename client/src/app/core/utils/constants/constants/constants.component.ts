import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class Constants {
  public readonly standardServerError = "Error interno en el servidor. Por favor, inténtelo más tarde.";
}
