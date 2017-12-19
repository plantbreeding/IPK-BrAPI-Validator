package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Homologous of config.Folder, groups Test results
 */
public class TestFolderReport {
    private List<TestItemReport> tests = new ArrayList<TestItemReport>();
    private String url;
    private String name;
    private String description;
    private int total;
    private int fails;
    private List<String> doneTests = new ArrayList<String>();
    private List<String> skippedTests = new ArrayList<String>();
    private List<String> missingReqsTests = new ArrayList<String>();

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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public void setFails(int fails) {
        this.fails = fails;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public int getFails() {
        return fails;
    }

	public List<String> getDoneTests() {
		return doneTests;
	}

	public void setDoneTests(List<String> doneTests) {
		this.doneTests = doneTests;
	}

	public List<String> getSkippedTests() {
		return skippedTests;
	}

	public void setSkippedTests(List<String> skippedTests) {
		this.skippedTests = skippedTests;
	}

	public List<String> getMissingReqsTests() {
		return missingReqsTests;
	}

	public void setMissingReqsTests(List<String> missingReqsTests) {
		this.missingReqsTests = missingReqsTests;
	}
}
