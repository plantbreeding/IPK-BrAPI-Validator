package org.brapi.brava.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A param is a pair of param and value for REST requests.
 */
@Getter
@Setter
@NoArgsConstructor
public class Param {
	private String param;
	private String value;
	private boolean array = false;
}
