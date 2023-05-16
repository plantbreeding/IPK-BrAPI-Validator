export interface ValidationRequest {
    strict: boolean ;
    accessToken: string ;
    async: boolean ;
}

export interface URLValidationRequest extends ValidationRequest  {
    url: string ;
    accessToken: string ;
    authorizationMethod: string ;
}