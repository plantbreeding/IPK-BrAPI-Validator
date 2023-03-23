package org.brapi.brava.core.validation;

import org.brapi.brava.core.model.Collection;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.reports.SuiteReport;

public interface SuiteValidator {

    /**
     * Run the validation
     *
     * @return Test report
     */
    public SuiteReport validate(boolean allowAdditional, Boolean singleTest, AuthorizationMethod authorizationMethod);

    /**
     * @return the url that was validated
     */
    public String getUrl();

    /**
     * @param resource the resource that was validated
     */
    public void setResource(Resource resource);

    void setCollection(Collection tc);
}
