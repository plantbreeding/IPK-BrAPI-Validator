package org.brapi.brava.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.brapi.brava.core.config.CollectionFactory;
import org.brapi.brava.core.exceptions.CollectionNotFound;
import org.brapi.brava.core.model.ValidationReport;
import org.brapi.brava.core.model.ValidationReportStatus;
import org.brapi.brava.core.reports.SuiteReport;
import org.brapi.brava.core.utils.ReportParser;
import org.brapi.brava.core.validation.AuthorizationMethod;
import org.brapi.brava.core.validation.CallSuiteValidator;

import java.util.Date;
import java.util.List;

public class ValidationService {

    private final CollectionFactory collectionFactory ;

    private final ReportParser reportParser ;

    public ValidationService(CollectionFactory collectionFactory,
                             ReportParser reportParser) {
        this.collectionFactory = collectionFactory;
        this.reportParser = reportParser;
    }

    public String getDefaultCollectionName() {
        return collectionFactory.getDefaultCollectionName() ;
    }

    public List<String> getCollectionNames() {
        return collectionFactory.getCollectionNames() ;
    }

    public ValidationReport validate(String resourceId,
                                     String url,
                                     String collectionName,
                                     boolean advancedMode,
                                     boolean strict,
                                     AuthorizationMethod authorizationMethod,
                                     String accessToken) throws CollectionNotFound, JsonProcessingException {
        CallSuiteValidator validator = new CallSuiteValidator(
                url,
                collectionFactory.getCollection(collectionName),
                advancedMode);

        boolean allowAdditional = !strict;

        SuiteReport suiteReport =
                validator.validate(allowAdditional, true, authorizationMethod, accessToken);

        return new ValidationReport(
                null,
                resourceId,
                url,
                collectionName,
                reportParser.writeAsString(suiteReport),
                suiteReport.getShortReport(),
                suiteReport.getMiniReport(),
                new Date(),
                ValidationReportStatus.COMPLETED,
                null);
    }
}
