package org.brapi.brava.jpa.service;

import org.brapi.brava.core.model.ValidationReport;
import org.brapi.brava.core.reports.SuiteReport;
import org.brapi.brava.core.utils.ReportParser;
import org.brapi.brava.data.service.ValidationReportService;
import org.brapi.brava.jpa.reports.ValidationReportEntity;
import org.brapi.brava.jpa.reports.ValidationReportRepository;
import org.brapi.brava.jpa.resources.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;

/**
 * JPA implementation of the ValidationReportService
 */
@Service
public class JPAValidationReportService implements ValidationReportService {

    private final ValidationReportRepository validationReportRepository ;

    private final ResourceRepository resourceRepository ;

    private final ReportParser reportParser ;

    public JPAValidationReportService(ValidationReportRepository validationReportRepository,
                                      ResourceRepository resourceRepository, ReportParser reportParser) {
        this.validationReportRepository = validationReportRepository;
        this.resourceRepository = resourceRepository;
        this.reportParser = reportParser;
    }

    @Override
    public ValidationReport save(ValidationReport report) {
        return convertToDTO(validationReportRepository.save(convertToEntity(report))) ;
    }

    @Override
    public Page<ValidationReport> findAllReports(Pageable pageable) {
        return validationReportRepository.findAll(pageable).map(this::convertToDTO) ;
    }

    private ValidationReport convertToDTO(ValidationReportEntity entity) {
        ValidationReport report = new ValidationReport() ;

        report.setReportId(entity.getId().toString());
        if (entity.getResource() != null) {
            report.setResourceId(entity.getResource().getId().toString());
        }
        report.setResourceUrl(entity.getUrl());
        report.setReportJson(entity.getReportJson());

        try {
            SuiteReport suiteReport = reportParser.readSuiteReport(entity.getReportJson());
            report.setShortReport(suiteReport.getShortReport());
            report.setMiniReport(suiteReport.getMiniReport());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        report.setDate(entity.getDate());

        return report ;
    }

    private ValidationReportEntity convertToEntity(ValidationReport report) {
        ValidationReportEntity entity = new ValidationReportEntity() ;

        entity.setId(UUID.fromString(report.getReportId()));

        // find any existing resource if possible
        if (report.getResourceId() != null) {
            entity.setResource(resourceRepository.
                    findById(UUID.fromString(report.getResourceId())).
                    orElseThrow(() -> new ResourceNotFoundRuntimeException(String.format("Can not find resource with id : %s", report.getResourceId()))));
        }
        // cache URL separately from resource in case the resource is URL is changed after the report was created
        entity.setUrl(report.getResourceUrl());
        entity.setReportJson(report.getReportJson());
        entity.setDate(report.getDate());

        return entity ;
    }
}
