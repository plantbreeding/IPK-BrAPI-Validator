package org.brapi.brava.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A request specifies url and method to call.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String url;
    private String method;
}
