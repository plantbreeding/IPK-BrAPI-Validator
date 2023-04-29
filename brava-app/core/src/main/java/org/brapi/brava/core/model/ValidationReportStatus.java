package org.brapi.brava.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ValidationReportStatus {
    EXECUTING ("executing"),
    COMPLETED ("completed"),
    FAILED ("failed") ;

    String name  ;
}