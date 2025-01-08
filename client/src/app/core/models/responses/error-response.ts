import { ApiResponse } from "./api-response";

export interface ErrorResponse extends ApiResponse {
    status: number;
    statusText: string;
}
