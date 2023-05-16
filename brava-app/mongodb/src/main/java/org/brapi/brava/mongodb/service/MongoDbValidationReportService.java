package org.brapi.brava.mongodb.service;

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
import org.brapi.brava.mongodb.reports.ValidationReportDocument;
import org.brapi.brava.mongodb.reports.ValidationReportDocumentRepository;
import org.brapi.brava.mongodb.resources.ResourceDocument;
import org.brapi.brava.mongodb.resources.ResourceDocumentRepository;
import org.bson.json.JsonObject;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Profile("mongodb")
@Service
public class MongoDbValidationReportService implements ValidationReportService {

    private final ValidationReportDocumentRepository validationReportRepository;

    private final ResourceDocumentRepository resourceRepository ;

    private final ResourceService resourceService ;

    private final ReportParser reportParser ;

    private final ValidationService validationService ;

    private final ExecutorService executorService ;

    public MongoDbValidationReportService(ValidationReportDocumentRepository validationReportRepository,
                                          ResourceDocumentRepository resourceRepository,
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
        return validationReportRepository.findAll(pageable).map(this::convertToModel) ;
    }

    @Override
    public ValidationReport findReport(String id) {
        return convertToModel(validationReportRepository.findById(UUID.fromString(id)).orElseThrow( () -> new EntityNotFoundRuntimeException(String.format("Can not find Report with id : %s ", id)))) ;
    }

    @Override
    public ValidationReport deleteReport(String id) throws EntityNotFoundException {
        try {
            ValidationReportDocument entity = validationReportRepository.findById(UUID.fromString(id)).orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find Report with id : %s ", id))) ;
            validationReportRepository.delete(entity) ;
            return convertToModel(entity) ;
        } catch (EntityNotFoundRuntimeException e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }

    private ValidationReport executeValidation(String resourceId,
                                               String url,
                                               String collectionName,
                                               boolean advancedMode,
                                               boolean strict,
                                               AuthorizationMethod authorizationMethod,
                                               String accessToken) throws ValidationException {
        try {
            return convertToModel(validationReportRepository.save(convertToDocument(validationService.validate(resourceId, url, collectionName, advancedMode, strict, authorizationMethod, accessToken)))) ;
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

        ValidationReportDocument document = new ValidationReportDocument();

        document.setUrl(url);
        document.setCollectionName(collectionName);
        document.setStatus(ValidationReportStatus.EXECUTING);

        try {
            // find any existing resource if possible
            if (resourceId != null) {
                ResourceDocument resourceDocument = resourceRepository.
                        findById(UUID.fromString(resourceId)).
                        orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find resource with id : %s", resourceId)));

                document.setResourceId(resourceDocument.getId());
            }
        } catch (EntityNotFoundRuntimeException e) {
            document.setStatus(ValidationReportStatus.FAILED);
            document.setExecutionError(e.getMessage());
            validationReportRepository.save(document) ;
            throw new ValidationException(e.getMessage(), e);
        }

        document = validationReportRepository.save(document) ;

        executorService.submit(new ValidationRunnable(document.getId(), resourceId, url, collectionName, advancedMode, strict, authorizationMethod, accessToken)) ;

        return convertToModel(document) ;
    }

    private ValidationReport convertToModel(ValidationReportDocument document) {
        ValidationReport report = new ValidationReport() ;

        report.setReportId(document.getId().toString());
        report.setResourceId(document.getResourceId().toString());

        report.setResourceUrl(document.getUrl());
        report.setCollectionName(document.getCollectionName());
        report.setReportJson(document.getReportJson().toString());

        try {
            SuiteReport suiteReport = reportParser.readSuiteReport(document.getReportJson().toString());
            report.setShortReport(suiteReport.getShortReport());
            report.setMiniReport(suiteReport.getMiniReport());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        report.setDate(document.getDate());

        return report ;
    }

    private ValidationReportDocument convertToDocument(ValidationReport report) {
        ValidationReportDocument document = new ValidationReportDocument() ;

        document.setId(UUID.fromString(report.getReportId()));
        document.setResourceId(UUID.fromString(report.getResourceId()));
        // cache URL separately from resource in case the resource is URL is changed after the report was created
        document.setUrl(report.getResourceUrl());
        document.setReportJson(new JsonObject(report.getReportJson()));
        document.setDate(report.getDate());

        return document ;
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

                ValidationReportDocument document = validationReportRepository.findById(reportId).orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find report with id : %s", reportId)));

                document.setStatus(ValidationReportStatus.COMPLETED);

                document.setReportJson(new JsonObject(report.getReportJson()));
                document.setDate(report.getDate());
                document.setStatus(report.getStatus());
                document.setExecutionError(null);

                validationReportRepository.save(document) ;
            } catch (CollectionNotFound | JsonProcessingException e) {
                ValidationReportDocument document = validationReportRepository.findById(reportId).orElseThrow(() -> new EntityNotFoundRuntimeException(String.format("Can not find report with id : %s", reportId)));

                document.setReportJson(null);
                document.setDate(new Date());
                document.setStatus(ValidationReportStatus.FAILED);
                document.setExecutionError(e.getMessage());

                validationReportRepository.save(document) ;
            }
        }
    }
}
