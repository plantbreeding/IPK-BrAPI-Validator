package org.brapi.brava.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Info object contains basic test collection information.
 */
@Getter
@Setter
@NoArgsConstructor
public class Info {
    private String name;
    private String description;
    private String schema;
}
