package org.brapi.brava.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * An Item is essentially one request and it accompanying tests.
 */
@Getter
@Setter
@NoArgsConstructor
public class Item {
    private String name;
    private String endpoint;
    private String description;
    private Request request;
    private List<Event> event;
	private List<String> requires;
	private List<Param> parameters;
}
