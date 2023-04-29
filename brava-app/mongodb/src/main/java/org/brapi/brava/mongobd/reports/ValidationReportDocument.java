package org.brapi.brava.mongobd.reports;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bson.json.JsonObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Document("validationReports")
public class ValidationReportDocument {
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
}
