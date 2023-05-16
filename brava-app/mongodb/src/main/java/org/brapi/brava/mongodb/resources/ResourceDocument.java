package org.brapi.brava.mongodb.resources;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.brava.core.model.ValidationFrequency;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.mongodb.DocumentWithId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@Document("resources")
public class ResourceDocument implements DocumentWithId {
    @NonNull
    @Id
    private UUID id ;
    @NonNull
    private String url ;
    @NonNull
    private AuthorizationMethod authorizationMethod = AuthorizationMethod.NONE ;
    private String crop;
    private String collectionName ;
    private String email;
    private ValidationFrequency frequency ;
    private boolean confirmed = false;
    private boolean isPublic = false;
    private String name ;
    private String description ;
    private UUID providerId;
    private String certificate ;
    private String logo ;
}
