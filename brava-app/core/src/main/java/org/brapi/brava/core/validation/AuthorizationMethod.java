package org.brapi.brava.core.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthorizationMethod {
    BEARER_HEADER("Authorization: Bearer", "Place the Authorization token as a 'Authorization' header with the prefix 'Bearer '"),
    URL_PARAMETER( "URL Parameter", "Place the Authorization token in the URL"),

    NONE( "None", "No Authorization required by the API") ;
    String name ;
    String description ;
}
