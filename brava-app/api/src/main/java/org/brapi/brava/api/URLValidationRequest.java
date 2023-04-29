package org.brapi.brava.api;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class URLValidationRequest extends ValidationRequest {
    String url ;
    String collectionName ;
    String authorizationMethod ;
}
