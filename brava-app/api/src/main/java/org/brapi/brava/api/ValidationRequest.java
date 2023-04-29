package org.brapi.brava.api;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ValidationRequest {
    Boolean strict ;
    String accessToken ;
    Boolean async ;
}
