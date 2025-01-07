import { ErrorResponse } from "./error-response";

export interface ValidationErrorResponse extends ErrorResponse {
    errors: Record<string, string>;
}
