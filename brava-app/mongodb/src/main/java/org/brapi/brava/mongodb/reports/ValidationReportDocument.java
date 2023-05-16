package org.brapi.brava.mongodb.reports;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.brava.core.model.ValidationReportStatus;
import org.brapi.brava.mongodb.DocumentWithId;
import org.bson.json.JsonObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Document("validationReports")
public class ValidationReportDocument implements DocumentWithId {
    @NonNull
    @Id
    private UUID id ;

    private UUID resourceId ;
    @NonNull
    private String url ;
    private String collectionName ;
    @NonNull
    private JsonObject reportJson ;
    @NonNull
    private Date date;
    @NonNull
    private ValidationReportStatus status;
    private String executionError;
}
