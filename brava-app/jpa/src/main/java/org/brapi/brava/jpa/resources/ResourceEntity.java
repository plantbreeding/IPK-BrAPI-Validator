package org.brapi.brava.jpa.resources;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
public class ResourceEntity {
    @Setter(AccessLevel.NONE)
    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id ;
    @NonNull
    private String url ;
}
