package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Homologous of config.Folder, groups Test results
 */
public class TestFolderReport {
	private List<TestItemReport> tests = new ArrayList<TestItemReport>();
	private String url;
	private VariableStorage variables;
	
	public TestFolderReport(String url) {
		this.url = url;
	}
	
	/**
	 * @param t Test report to be added to the folder
	 */
	public void addTestReport(TestItemReport t) {
		this.tests.add(t);
	}
	public List<TestItemReport> getTests() {
		return tests;
	}

	public String getUrl() {
		return url;
	}

	/**
	 * @param variables VariableStorage assigned to this folder.
	 */
	public void setVariables(VariableStorage variables) {
		this.variables = variables;
	}

	public VariableStorage getVariables() {
		return variables;
	}
}
