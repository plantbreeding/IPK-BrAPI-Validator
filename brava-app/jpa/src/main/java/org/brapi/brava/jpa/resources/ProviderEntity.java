package org.brapi.brava.jpa.resources;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
public class ProviderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id ;
    @NonNull
    private String name;
    private String email;
    private String description;
    private String logo;
}
