package org.brapi.brava.core.reports;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
/**
 * Summary information on the Suite Report
 */
public class MiniReport {
	double time;
	List<String> totalTests;
	List<String> passedTests;
	List<String> failedTests;
	List<String> warningTests;

	public MiniReport (Map<String, Map<String, Object>> shortReport) {
		List<Integer> time = new ArrayList<Integer>();
		List<String> totalTests = new ArrayList<String>();
		List<String> passedTests = new ArrayList<String>();
		List<String> warningTests = new ArrayList<String>();
		List<String> failedTests = new ArrayList<String>();
		for (Map.Entry<String, Map<String, Object>> folder : shortReport.entrySet()) {
			for(Map.Entry<String, Object> tests : folder.getValue().entrySet()) {
				if (tests.getValue().getClass().equals(LinkedHashMap.class)) {
					Map<String, Object> test = (Map<String, Object>) tests.getValue();
					if (!tests.getKey().equals("/calls")) {
						time.add((int) test.get("responseTime"));
					}
					totalTests.add(tests.getKey());
					List<String> testStatus = (List<String>) test.get("testStatus");
					if (testStatus.isEmpty()) {
						passedTests.add(tests.getKey());
					} else {
						boolean failed = false;
						// Count as failed only if it fails for the following reasons.
						for (String reason : testStatus) {
							if (reason.equals("wrong status code") ||
									reason.equals("wrong contentType") ||
									reason.equals("can't connect")) {
								failed = true;
							}
						}
						if (failed) {
							failedTests.add(tests.getKey());
						} else {
							warningTests.add(tests.getKey());
						}


					}
				}
			}
		}
		double median;
		if (time.isEmpty()) {
			median = 0;
		} else {
			median = calculateMedian(time);
		}
		setTime(median);
		setPassedTests(passedTests);
		setTotalTests(totalTests);
		setFailedTests(failedTests);
		setWarningTests(warningTests);
	}


	private double calculateMedian(List<Integer> l) {
		Collections.sort(l);
		double median;
		if (l.size() % 2 == 0) {
			median = (l.get(l.size()/2) + l.get(l.size()/2 - 1))/2;
		} else {
			median = l.get(l.size()/2);
		}
		return median;
	}
}
