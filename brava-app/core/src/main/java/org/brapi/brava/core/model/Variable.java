package org.brapi.brava.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Variable entity that contains some predefined variable coming from the test collection json file.
 * Currently, the variables included in this object are not taken into account for the tests.
 */
@Getter
@Setter
@NoArgsConstructor
public class Variable {
    private String id;
    private String key;
    private Object value;
    private String type;
    private String name;
    private String description;
}
