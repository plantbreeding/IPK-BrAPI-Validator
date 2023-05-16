package org.brapi.brava.mongodb;

import java.util.UUID;

public interface DocumentWithId {
    UUID getId() ;

    void setId(UUID id) ;
}
