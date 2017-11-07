package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains multiple TestCollectionReports and the base url to be used (endpoint url) in those collections.
 */
public class TestSuiteReport {
	private List<TestCollectionReport> testCollections = new ArrayList<TestCollectionReport>();
	private int id;
	private String baseUrl;
	
	public TestSuiteReport(int id, String url) {
		setId(id);
		setBaseUrl(url); 
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBaseUrl() {
		return baseUrl;
	}


	public void setBaseUrl(String url) {
		baseUrl = url;
	}
	
	public void addTestCollectionReport(TestCollectionReport tc) {
		this.testCollections.add(tc);
	}

	public List<TestCollectionReport> getTestCollections() {
		return testCollections;
	}

	
}
