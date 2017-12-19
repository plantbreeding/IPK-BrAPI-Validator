package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about one config. Item collection of tests (ExecReports). Mostly test results and
 * some information about the request.
 */
public class TestItemReport {
    private String name;
    private List<TestExecReport> test = new ArrayList<TestExecReport>();
    private String endpoint;
    private String method;
    private boolean cached;
    private boolean allPassed;

    public TestItemReport(String name, String endpoint, String method) {
        setName(name);
        setEndpoint(endpoint);
        setMethod(method);

    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the messageList
     */
    public List<TestExecReport> getTest() {
        return test;
    }


    /**
     * @param test Test report to be added.
     */
    public void addTest(TestExecReport test) {
        this.test.add(test);
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public boolean isCached() {
        return cached;
    }

	/**
	 * @return the allPassed
	 */
	public boolean isAllPassed() {
		return allPassed;
	}

	/**
	 * @param allPassed the allPassed to set
	 */
	public void setAllPassed(boolean allPassed) {
		this.allPassed = allPassed;
	}

}
