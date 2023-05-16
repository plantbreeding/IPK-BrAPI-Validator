package org.brapi.brava.mongodb.providers;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.brava.mongodb.DocumentWithId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@Document("providers")
public class ProviderDocument implements DocumentWithId {
    @NonNull
    @Id
    private UUID id ;
    @NonNull
    private String name;
    private String email;
    private String description;
    private String logo;
}
