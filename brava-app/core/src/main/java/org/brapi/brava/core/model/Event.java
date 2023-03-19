package org.brapi.brava.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Contains the list of commands for a given Item.
 */
@Getter
@Setter
@NoArgsConstructor
public class Event {
    private String listen;
    private String type;
    private List<String> exec;
}
