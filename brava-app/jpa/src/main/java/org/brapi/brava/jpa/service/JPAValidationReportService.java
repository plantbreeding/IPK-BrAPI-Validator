package org.brapi.brava.jpa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.model.ValidationReport;
import org.brapi.brava.core.model.ValidationReportStatus;
import org.brapi.brava.core.reports.SuiteReport;
import org.brapi.brava.core.service.ValidationService;
import org.brapi.brava.core.utils.ReportParser;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.exceptions.EntityNotFoundRuntimeException;
import org.brapi.brava.data.service.ResourceService;
import org.brapi.brava.data.service.ValidationException;
import org.brapi.brava.data.service.ValidationReportService;
import org.brapi.brava.jpa.reports.ValidationReportEntity;
import org.brapi.brava.jpa.reports.ValidationReportRepository;
import org.brapi.brava.jpa.resources.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * JPA implementation of the ValidationReportService
 */
@Service
public class JPAValidationReportService implements ValidationReportService {

    private final ValidationReportRepository validationReportRepository ;

    private final ResourceRepository resourceRepository ;

    private final ResourceService resourceService ;

    private final ReportParser reportParser ;

    private final ValidationService validationService ;

    private final ExecutorService executorService ;

    public JPAValidationReportService(ValidationReportRepository validationReportRepository,
                                      ResourceRepository resourceRepository,
                                      ResourceService resourceService,
                                      ReportParser reportParser,
                                      ValidationService validationService,
                                      ExecutorService executorService) {
        this.validationReportRepository = validationReportRepository;
        this.resourceRepository = resourceRepository;
        this.resourceService = resourceService;
        this.reportParser = reportParser;
        this.validationService = validationService;
        this.executorService = executorService;
    }

    @Override
    public ValidationReport executeValidation(String url,
                                              String collectionName,
                                              boolean advancedMode,
                                              boolean strict,
                                              AuthorizationMethod authorizationMethod,
                                              String accessToken) throws ValidationException {

        return executeValidation(null, url, collectionName, advancedMode, strict, authorizationMethod, accessToken) ;
    }

    public ValidationReport executeValidation(String resourceId, boolean advancedMode, Boolean strict, String accessToken) throws ValidationException, EntityNotFoundException {
        Resource resource = resourceService.findResource(resourceId);

        return executeValidation(resource.getId(), resource.getUrl(), resource.getCollectionName(), advancedMode, strict, resource.getAuthorizationMethod(), accessToken) ;
    }

    @Override
    public ValidationReport submitValidation(String url,
                                             String collectionName,
                                             boolean advancedMode,
                                             boolean strict,
                                             AuthorizationMethod authorizationMethod,
                                             String accessToken) throws ValidationException {

        return submitValidation(null, url, collectionName, advancedMode, strict, authorizationMethod, accessToken) ;
    }

    public ValidationReport submitValidation(String resourceId, boolean advancedMode, Boolean strict, String accessToken) throws ValidationException, EntityNotFoundException {
        Resource resource = resourceService.findResource(resourceId);

        return submitValidation(resource.getId(), resource.getUrl(), resource.getCollectionName(), advancedMode, strict, resource.getAuthorizationMethod(), accessToken) ;
    }

    @Override
    public Page<ValidationReport> findAllReports(Pageable pageable) {
        return validationReportRepository.findAll(pageable).map(this::convertToDTO) ;
    }

    @Override
    public ValidationReport findReport(String id) {
        return convertToDTO(validationReportRepository.findById(UUID.fromString(id)).orElseThrow( () -> new EntityNotFoundRuntimeException(String.format("Can not find Report with id : %s ", id)))) ;
    }

    private ValidationReport executeValidation(String resourceId,
                                               String url,
                                               String collectionName,
                                               boolean advancedMode,
                                               boolean strict,
                                               AuthorizationMethod authorizationMethod,
                                               String accessToken) throws ValidationException {
        try {
            return convertToDTO(validationReportRepository.save(convertToEntity(validationService.validate(resourceId, url, collectionName, advancedMode, strict, authorizationMethod, accessToken)))) ;
        } catch (CollectionNotFound e) {
            throw new ValidationException(String.format("Unknown collection '%s', must be one of %s", collectionName, validationService.getCollectionNames()), e);
        } catch (JsonProcessingException e) {
            throw new ValidationException("Can not convert the report to json", e);
        }
    }

    private ValidationReport submitValidation(String resourceId,
                                               String url,
                                               String collectionName,
                                               boolean advancedMode,
                                               boolean strict,
                                               AuthorizationMethod authorizationMethod,
                                               String accessToken) throws ValidationException {

        ValidationReportEntity entity = new ValidationReportEntity();

        entity.setResourceUrl(url);
        entity.setCollectionName(collectionName);
        entity.setStatus(ValidationReportStatus.EXECUTING);

        try {
            // find any existing resource if possible
            if (resourceId != null) {
                entity.setResource(resourceRepository.
                        findById(UUID.fromString(resourceId)).
                        orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find resource with id : %s", resourceId))));
            }
        } catch (EntityNotFoundRuntimeException e) {
            entity.setStatus(ValidationReportStatus.FAILED);
            entity.setExecutionError(e.getMessage());
            validationReportRepository.save(entity) ;
            throw new ValidationException(e.getMessage(), e);
        }

        entity = validationReportRepository.save(entity) ;

        executorService.submit(new ValidationRunnable(entity.getId(), resourceId, url, collectionName, advancedMode, strict, authorizationMethod, accessToken)) ;

        return convertToDTO(entity) ;
    }

    private ValidationReport convertToDTO(ValidationReportEntity entity) {
        ValidationReport report = new ValidationReport() ;

        report.setReportId(entity.getId().toString());
        if (entity.getResource() != null) {
            report.setResourceId(entity.getResource().getId().toString());
        }
        report.setResourceUrl(entity.getResourceUrl());
        report.setCollectionName(entity.getCollectionName());
        report.setReportJson(entity.getReportJson());

        if (entity.getReportJson() != null) {
            try {
                SuiteReport suiteReport = reportParser.readSuiteReport(entity.getReportJson());
                report.setShortReport(suiteReport.getShortReport());
                report.setMiniReport(suiteReport.getMiniReport());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage(), e); // this should not happen because the json saved in the repository should be valid
            }
        }

        report.setDate(entity.getDate());
        report.setStatus(entity.getStatus());
        report.setExecutionError(entity.getExecutionError());

        return report ;
    }

    private ValidationReportEntity convertToEntity(ValidationReport report) {
        ValidationReportEntity entity = new ValidationReportEntity() ;

        if (report.getReportId() != null) {
            entity.setId(UUID.fromString(report.getReportId()));
        }

        // find any existing resource if possible
        if (report.getResourceId() != null) {
            entity.setResource(resourceRepository.
                    findById(UUID.fromString(report.getResourceId())).
                    orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find resource with id : %s", report.getResourceId()))));
        }
        // cache URL separately from resource in case the resource is URL is changed after the report was created
        entity.setCollectionName(report.getCollectionName());
        entity.setResourceUrl(report.getResourceUrl());
        entity.setReportJson(report.getReportJson());
        entity.setDate(report.getDate());
        entity.setStatus(report.getStatus());
        entity.setExecutionError(report.getExecutionError());

        return entity ;
    }

    @AllArgsConstructor
    private class ValidationRunnable implements Runnable {
        private UUID reportId ;
        private String resourceId ;
        private String url ;
        private String collectionName ;
        private boolean advancedMode ;
        private boolean strict ;
        private AuthorizationMethod authorizationMethod ;
        private String accessToken ;

        @Override
        public void run() {
            try {
                ValidationReport report = validationService.validate(resourceId, url, collectionName, advancedMode, strict, authorizationMethod, accessToken);

                ValidationReportEntity entity = validationReportRepository.findById(reportId).orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find report with id : %s", reportId)));

                entity.setStatus(ValidationReportStatus.COMPLETED);

                entity.setReportJson(report.getReportJson());
                entity.setDate(report.getDate());
                entity.setStatus(report.getStatus());
                entity.setExecutionError(null);

                validationReportRepository.save(entity) ;
            } catch (CollectionNotFound | JsonProcessingException e) {
                ValidationReportEntity entity = validationReportRepository.findById(reportId).orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find report with id : %s", reportId)));

                entity.setReportJson(null);
                entity.setDate(new Date());
                entity.setStatus(ValidationReportStatus.FAILED);
                entity.setExecutionError(e.getMessage());

                validationReportRepository.save(entity) ;
            }
        }
    }
}
