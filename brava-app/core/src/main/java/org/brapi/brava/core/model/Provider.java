package org.brapi.brava.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.UUID;

/**
 * A provider instance contains the information related to one resource provider
 */
@Getter
@Setter
public class Provider {

    private UUID id;
    private String name;
    private String description;
    private String logo;

    @JsonIgnore
    private Collection<Resource> resources;
}
