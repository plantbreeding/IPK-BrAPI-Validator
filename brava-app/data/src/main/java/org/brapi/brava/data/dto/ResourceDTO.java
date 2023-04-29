package org.brapi.brava.data.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.brava.core.model.ValidationFrequency;
import org.brapi.brava.core.validation.AuthorizationMethod;

@Getter
@Setter
@NoArgsConstructor
public class ResourceDTO {
    @NonNull
    private String url;
    @NonNull
    private AuthorizationMethod authorizationMethod = AuthorizationMethod.NONE  ;
    private String collectionName ;
    private String crop;
    private String email;
    private ValidationFrequency frequency = null;
    private boolean confirmed = false;
    private boolean isPublic = false;
    private String name ;
    private String description ;
    private String providerId;
    private String certificate ;
    private String logo ;
}
