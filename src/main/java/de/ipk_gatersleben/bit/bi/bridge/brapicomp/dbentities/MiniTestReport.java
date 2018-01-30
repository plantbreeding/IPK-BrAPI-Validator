package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.util.List;

public class MiniTestReport {
	double time;
	List<String> totalTests;
	List<String> passedTests;
	List<String> failedTests;
	List<String> warningTests;
	
	public MiniTestReport() {
		super();
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public List<String> getTotalTests() {
		return totalTests;
	}
	public void setTotalTests(List<String> totalTests) {
		this.totalTests = totalTests;
	}
	public List<String> getPassedTests() {
		return passedTests;
	}
	public void setPassedTests(List<String> passedTests) {
		this.passedTests = passedTests;
	}
	public List<String> getFailedTests() {
		return failedTests;
	}
	public void setFailedTests(List<String> failedTests) {
		this.failedTests = failedTests;
	}

	public List<String> getWarningTests() {
		return warningTests;
	}

	public void setWarningTests(List<String> warningTests) {
		this.warningTests = warningTests;
	}
}
