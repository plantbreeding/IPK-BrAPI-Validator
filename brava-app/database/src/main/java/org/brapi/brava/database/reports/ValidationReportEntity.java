package org.brapi.brava.database.reports;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
public class ValidationReportEntity {
    @Setter(AccessLevel.NONE)
    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reportId ;
    @NonNull
    private String resourceUrl ;
    @NonNull
    private String reportJson ;
    @NonNull
    private Date date;
}
