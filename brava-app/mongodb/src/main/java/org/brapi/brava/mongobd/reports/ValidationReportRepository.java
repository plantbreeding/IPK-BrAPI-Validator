package org.brapi.brava.mongobd.reports;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ValidationReportRepository extends MongoRepository<ValidationReportDocument, UUID> {
}
