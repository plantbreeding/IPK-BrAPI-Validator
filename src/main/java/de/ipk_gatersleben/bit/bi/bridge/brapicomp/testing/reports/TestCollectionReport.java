package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Report object that contains the tests results and various messages, organized by folders.
 * This can be exported as Json
 */
public class TestCollectionReport {
	private List<TestFolderReport> folders = new ArrayList<TestFolderReport>();
	private String url;
	private String name;
	
	public TestCollectionReport(String name, String url) {
		this.url = url;
		this.name = name;
	}
	
	public void addFolder(TestFolderReport f) {
		this.folders.add(f);
	}

	/**
	 * @return the tests
	 */
	public List<TestFolderReport> getFolders() {
		return folders;
	}

	/**
	 * @return The url of the endpoint
	 */
	public String getUrl() {
		return url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
