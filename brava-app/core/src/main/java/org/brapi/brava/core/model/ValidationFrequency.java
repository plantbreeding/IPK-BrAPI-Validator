package org.brapi.brava.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ValidationFrequency {
    DAILY ("daily"),
    WEEKLY ("weekly"),
    MONTHLY ("monthly") ;

    String name  ;
}
