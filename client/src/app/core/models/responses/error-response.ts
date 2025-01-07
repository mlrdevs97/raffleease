import { ApiResponse } from "./api-response";

export interface ErrorResponse extends ApiResponse {
    errorCode: number;
    reason: string;
}
