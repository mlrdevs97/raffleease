import { ApiResponse } from "./api-response";

export interface SuccessResponse<T> extends ApiResponse {
    data: T | null;
}
