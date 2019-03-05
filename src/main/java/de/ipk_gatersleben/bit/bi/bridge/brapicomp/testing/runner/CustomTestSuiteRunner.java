package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;


import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestCollectionReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;


/**
 * Runs a test collection config instance against an endpoint.
 */
public class CustomTestSuiteRunner implements TestSuiteRunner {

    private String id;
    private Resource ep;
    private TestCollection testCollection;

    /**
     * Constructor
     *
     * @param string Some id to set for this test run
     * @param url    Base URL of the endpoint to be tested
     * @param tc     TestCollection config instance.
     */
    public CustomTestSuiteRunner(String string, Resource ep, TestCollection tc) {
        this.setId(string);
        this.setEp(ep);
        this.setTestCollection(tc);
    }

    /**
     * Run the tests
     *
     * @return Test report
     */
    public TestSuiteReport runTests(boolean allowAdditional, Boolean singleTest) {
        TestSuiteReport testSuiteReport = new TestSuiteReport(id, ep);
        TestCollectionRunner testCollectionRunner = new TestCollectionRunner(testCollection, ep);
        TestCollectionReport tcr = testCollectionRunner.runTests(allowAdditional);
        testSuiteReport.addTestCollectionReport(tcr);
        return testSuiteReport;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    public Resource getEp() {
        return ep;
    }
    
    @Override
    public void setEp(Resource ep) {
        this.ep = ep;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return ep.getUrl();
    }


    public void setTestCollection(TestCollection tc) {
        this.testCollection = tc;
    }

}
