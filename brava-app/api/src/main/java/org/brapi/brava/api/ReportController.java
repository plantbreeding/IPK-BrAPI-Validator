package org.brapi.brava.api;

import org.brapi.brava.core.model.ValidationReport;
import org.brapi.brava.data.service.ValidationReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
public class ReportController {

    private final ValidationReportService validationReportService ;

    public ReportController(ValidationReportService validationReportService) {
        this.validationReportService = validationReportService;
    }

    @GetMapping(path = "/reports")
    public Page<ValidationReport> reports(Pageable pageable) {
        return validationReportService.findAllReports(pageable);
    }
}
