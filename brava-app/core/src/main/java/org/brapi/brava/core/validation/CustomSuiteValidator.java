package org.brapi.brava.core.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.brapi.brava.core.model.Collection;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.reports.CollectionReport;
import org.brapi.brava.core.reports.SuiteReport;


/**
 * Runs a test collection config instance against an endpoint.
 */
@Getter
@Setter
@AllArgsConstructor
public class CustomSuiteValidator implements SuiteValidator {

    private Resource resource;
    private Collection collection;

    private boolean advancedMode;

    /**
     * Run the validation on the Validation Collection
     *
     * @param allowAdditional
     * @param singleTest          <code>true</code> if the validation is part of a single test,
     *                            or <code>false</code> if part of a suite of tests
     * @param authorizationMethod
     * @return The Validation Report for the collection
     */
    public SuiteReport validate(boolean allowAdditional, Boolean singleTest, AuthorizationMethod authorizationMethod) {
        SuiteReport suiteReport = new SuiteReport(resource);
        CollectionValidator collectionValidator = new CollectionValidator(resource, collection, advancedMode) ;
        CollectionReport collectionReport = collectionValidator.validate(allowAdditional, authorizationMethod);
        suiteReport.addCollectionReport(collectionReport);
        return suiteReport;
    }

    /**
     * @return the url from the resource
     */
    @Override
    public String getUrl() {
        return resource.getUrl();
    }
}
