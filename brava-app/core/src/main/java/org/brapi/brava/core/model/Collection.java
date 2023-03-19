package org.brapi.brava.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Validation collection configuration (Renamed from TestCollection to avoid confusion with java unit tests). Used to define which endpoints to call and validations to run.
 * The Json config file is deserialized into this.
 */
@Getter
@Setter
@NoArgsConstructor
public class Collection {
    private Info info;
    private List<Folder> item;
}
