package org.brapi.brava.core.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * A provider instance contains the information related to one resource provider
 */
@Getter
@Setter
@RequiredArgsConstructor
public class Provider {
    @NonNull
    private String id;
    @NonNull
    private String name;
    private String email;
    private String description;
    private String logo;
}
