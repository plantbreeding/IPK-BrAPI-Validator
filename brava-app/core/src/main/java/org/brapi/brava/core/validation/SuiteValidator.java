package org.brapi.brava.core.validation;

import org.brapi.brava.core.model.Collection;
import org.brapi.brava.core.reports.SuiteReport;

public interface SuiteValidator {

    /**
     * Run the validation on the Validation Collection
     *
     * @param allowAdditional
     * @param singleTest          <code>true</code> if the validation is part of a single test,
     *                            or <code>false</code> if part of a suite of tests
     * @param authorizationMethod The method by which the accessToken is sent to the server
     * @param accessToken the access token if authorizationMethod is not {{@link AuthorizationMethod#NONE}}
     *
     * @return The Validation Report for the collection
     */
    public SuiteReport validate(boolean allowAdditional, Boolean singleTest, AuthorizationMethod authorizationMethod, String accessToken);

    /**
     * @return the url that was validated
     */
    public String getUrl();

    /**
     * @param url the url that was validated
     */
    public void setUrl(String url);

    void setCollection(Collection tc);
}
