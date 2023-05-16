package org.brapi.brava.mongodb.reports;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ValidationReportDocumentRepository extends MongoRepository<ValidationReportDocument, UUID> {
}
