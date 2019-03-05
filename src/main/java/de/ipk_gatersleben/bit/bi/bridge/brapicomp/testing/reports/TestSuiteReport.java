package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports;

import java.util.ArrayList;
import java.util.List;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Resource;

/**
 * Contains multiple TestCollectionReports and the base url to be used (endpoint url) in those collections.
 */
public class TestSuiteReport {

	private List<TestCollectionReport> testCollections = new ArrayList<TestCollectionReport>();
    private String id;
    private Resource ep;

    public TestSuiteReport(String id, Resource ep) {
        setId(id);
        setEp(ep);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBaseUrl() {
        return ep.getUrl();
    }


    public void setEp(Resource ep) {
        this.ep = ep;
    }

    public void addTestCollectionReport(TestCollectionReport tc) {
        this.testCollections.add(tc);
    }

    public List<TestCollectionReport> getTestCollections() {
        return testCollections;
    }


}
