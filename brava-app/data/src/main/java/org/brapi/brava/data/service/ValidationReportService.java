package org.brapi.brava.data.service;

import org.brapi.brava.core.model.ValidationReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for persisting ValidationReports
 */
public interface ValidationReportService {
    ValidationReport save(ValidationReport report);

    Page<ValidationReport> findAllReports(Pageable pageable);
}
