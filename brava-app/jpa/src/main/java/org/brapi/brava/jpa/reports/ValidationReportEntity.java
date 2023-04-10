package org.brapi.brava.jpa.reports;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.brava.jpa.resources.ResourceEntity;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
public class ValidationReportEntity {
    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id ;
    @ManyToOne
    private ResourceEntity resource ;
    @NonNull
    private String url ;
    @NonNull
    @Lob
    private String reportJson ;
    @NonNull
    private Date date;
}
