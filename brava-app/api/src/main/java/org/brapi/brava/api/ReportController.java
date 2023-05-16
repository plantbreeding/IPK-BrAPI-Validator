package org.brapi.brava.api;

import org.brapi.brava.core.model.ValidationReport;
import org.brapi.brava.data.exceptions.EntityNotFoundException;
import org.brapi.brava.data.service.ValidationReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ReportController {

    private final ValidationReportService validationReportService ;

    public ReportController(ValidationReportService validationReportService) {
        this.validationReportService = validationReportService;
    }

    @GetMapping(path = "/reports")
    public Page<ValidationReport> getAllValidationReports(Pageable pageable) {
        return validationReportService.findAllReports(pageable);
    }

    @GetMapping(path = "/reports/{id}")
    public ValidationReport getValidationReport(@PathVariable String id) {
        try {
            return validationReportService.findReport(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Report id : '%s' is not a valid UUID", id), e);
        }
    }

    @DeleteMapping(path = "/reports/{id}")
    public ValidationReport deleteValidationReport(@PathVariable String id) {
        try {
            return validationReportService.deleteReport(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format("Resource is not valid due to %s", e.getMessage()), e);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, String.format(e.getMessage()));
        }
    }
}
