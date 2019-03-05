package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;


import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestCollectionReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;


/**
 * Runs a test against all recognized endpoints found in /call instance against an endpoint.
 */
public class CallTestSuiteRunner implements TestSuiteRunner {

    private String id;
    private Resource ep;
    private TestCollection testCollection;

    /**
     * Constructor
     *
     * @param string Some id to set for this test run
     * @param ep    Base URL of the endpoint to be tested
     * @param tc 
     */
    public CallTestSuiteRunner(String string, Resource ep, TestCollection tc) {
        this.setId(string);
        this.setEp(ep);
        this.setTestCollection(tc);
    }

    /**
     * Run the tests
     * @param singleTest 
     *
     * @return Test report
     */
    public TestSuiteReport runTests(boolean allowAdditional, Boolean singleTest) {
        TestSuiteReport testSuiteReport = new TestSuiteReport(id, ep);
        TestCollectionRunner testCollectionRunner = new TestCollectionRunner(testCollection, ep);
        TestCollectionReport tcr = testCollectionRunner.runTestsFromCall(allowAdditional, singleTest);
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
