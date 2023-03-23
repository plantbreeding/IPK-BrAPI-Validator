package org.brapi.brava.database.reports;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
public class ValidationReportEntity {
    @NonNull
    private String reportId ;
    @NonNull
    private String resourceUrl ;
    @NonNull
    private String reportJson ;
    @NonNull
    private Date date;
}
