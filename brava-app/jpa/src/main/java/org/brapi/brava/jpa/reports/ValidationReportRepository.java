package org.brapi.brava.jpa.reports;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ValidationReportRepository extends CrudRepository<ValidationReportEntity, UUID>, PagingAndSortingRepository<ValidationReportEntity, UUID> {
}
