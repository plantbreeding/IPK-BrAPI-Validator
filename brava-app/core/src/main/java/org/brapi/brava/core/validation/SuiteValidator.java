package org.brapi.brava.core.validation;

import org.brapi.brava.core.model.Collection;
import org.brapi.brava.core.model.Resource;
import org.brapi.brava.core.reports.SuiteReport;

public interface SuiteValidator {

    /**
     * Run the tests
     *
     * @return Test report
     */
    public SuiteReport validate(boolean allowAdditional, Boolean singleTest, AuthorizationMethod authorizationMethod);

    /**
     * @return the id
     */
    public String getId();

    /**
     * @param id the id to set
     */
    public void setId(String id);
    /**
     * @return the url
     */
    public String getUrl();

    /**
     * @param url the url to set
     */
    public void setResource(Resource resource);

    void setCollection(Collection tc);
}
