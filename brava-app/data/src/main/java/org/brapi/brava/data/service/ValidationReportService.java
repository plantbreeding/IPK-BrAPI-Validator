package org.brapi.brava.data.service;

import org.brapi.brava.core.model.ValidationReport;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for persisting ValidationReports
 */
public interface ValidationReportService {
    ValidationReport executeValidation(String url,
                                       String collectionName,
                                       boolean advancedMode,
                                       boolean strict,
                                       AuthorizationMethod authorizationMethod,
                                       String accessToken) throws ValidationException ;

    ValidationReport executeValidation(String resourceId, boolean advancedMode, Boolean strict, String accessToken) throws ValidationException, EntityNotFoundException;

    ValidationReport submitValidation(String url,
                                      String collectionName,
                                      boolean advancedMode,
                                      boolean strict,
                                      AuthorizationMethod authorizationMethod,
                                      String accessToken) throws ValidationException ;

    ValidationReport submitValidation(String resourceId, boolean advancedMode, Boolean strict, String accessToken) throws ValidationException, EntityNotFoundException;

    Page<ValidationReport> findAllReports(Pageable pageable);
    ValidationReport findReport(String id) throws EntityNotFoundException;

    ValidationReport deleteReport(String id) throws EntityNotFoundException;
}
