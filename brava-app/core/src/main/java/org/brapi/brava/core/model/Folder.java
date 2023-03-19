package org.brapi.brava.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * A folder contains multiple Items, which contain the tests themselves.
 * Folders are used to conceptually organize tests.
 */
@Getter
@Setter
@NoArgsConstructor
public class Folder {
    private String name;
    private String description;
    private List<Item> item;
    private List<Variable> variable;
}
