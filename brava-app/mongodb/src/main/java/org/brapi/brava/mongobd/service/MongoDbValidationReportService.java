package org.brapi.brava.mongobd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.brapi.brava.core.model.ValidationReport;
import org.brapi.brava.core.reports.SuiteReport;
import org.brapi.brava.core.utils.ReportParser;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.exceptions.EntityNotFoundRuntimeException;
import org.brapi.brava.data.service.ValidationException;
import org.brapi.brava.data.service.ValidationReportService;
import org.brapi.brava.mongobd.reports.ValidationReportDocument;
import org.brapi.brava.mongobd.reports.ValidationReportRepository;
import org.bson.json.JsonObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MongoDbValidationReportService implements ValidationReportService {

    private final ValidationReportRepository validationReportRepository ;
    private final ReportParser reportParser ;

    public MongoDbValidationReportService(ValidationReportRepository validationReportRepository, ReportParser reportParser) {
        this.validationReportRepository = validationReportRepository;
        this.reportParser = reportParser;
    }



    public ValidationReport saveReport(ValidationReport report) {
        return convertToDTO(validationReportRepository.save(convertToDocument(report)));
    }

    @Override
    public ValidationReport executeValidation(String url, String collectionName, boolean advancedMode, boolean strict, AuthorizationMethod authorizationMethod, String accessToken) throws ValidationException {
        return null;
    }

    @Override
    public ValidationReport executeValidation(String resourceId, boolean advancedMode, Boolean strict, String accessToken) throws ValidationException, EntityNotFoundException {
        return null;
    }

    @Override
    public ValidationReport submitValidation(String url, String collectionName, boolean advancedMode, boolean strict, AuthorizationMethod authorizationMethod, String accessToken) throws ValidationException {
        return null;
    }

    @Override
    public ValidationReport submitValidation(String resourceId, boolean advancedMode, Boolean strict, String accessToken) throws ValidationException, EntityNotFoundException {
        return null;
    }

    @Override
    public Page<ValidationReport> findAllReports(Pageable pageable) {
        return validationReportRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public ValidationReport findReport(String id) {
        return convertToDTO(validationReportRepository.findById(UUID.fromString(id)).orElseThrow( () -> new EntityNotFoundRuntimeException(String.format("Can not find Report with id : %s ", id)))) ;
    }

    private ValidationReport convertToDTO(ValidationReportDocument document) {
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
}
