package org.brapi.brava.core.reports;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
/**
 * Summary information on the report
 */
public class MiniReport {
	double time;
	List<String> totalTests;
	List<String> passedTests;
	List<String> failedTests;
	List<String> warningTests;
}
