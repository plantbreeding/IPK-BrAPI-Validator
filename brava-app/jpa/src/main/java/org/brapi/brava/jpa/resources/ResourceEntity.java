package org.brapi.brava.jpa.resources;

import jakarta.persistence.*;
import lombok.*;
import org.brapi.brava.core.model.ValidationFrequency;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.jpa.providers.ProviderEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
public class ResourceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @ManyToOne
    private ProviderEntity provider;
    private String certificate ;
    private String logo ;
}
