package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner;



import java.util.logging.Logger;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestCollectionReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;


/**
 * Runs a test collection config instance against an endpoint.
 */
public class TestSuiteRunner {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(TestSuiteRunner.class.getName());
	private int id;
	private String url;
	private TestCollection testCollection;
	
	/**
	 * Constructor
	 * @param id Some id to set for this test run
	 * @param url Base URL of the endpoint to be tested
	 * @param tc TestCollection config instance.
	 */
	public TestSuiteRunner(int id, String url, TestCollection tc) {
		this.setId(id);
		this.setUrl(url);
		this.setTestCollection(tc);
	}
	
	/**
	 * Run the tests
	 * @return Test report
	 */
	public TestSuiteReport runTests() {
		TestSuiteReport testSuiteReport = new TestSuiteReport(id, url);
		TestCollectionRunner testCollectionRunner = new TestCollectionRunner(testCollection, url);
		TestCollectionReport tcr = testCollectionRunner.runTests();
		testSuiteReport.addTestCollectionReport(tcr);		
		return testSuiteReport;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	public TestCollection getTestCollection() {
		return testCollection;
	}

	public void setTestCollection(TestCollection tc) {
		this.testCollection = tc;
	}
	
}
