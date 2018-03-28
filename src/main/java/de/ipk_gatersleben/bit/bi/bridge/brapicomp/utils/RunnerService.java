package de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.EmailManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.*;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Item;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.*;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner.TestItemRunner;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner.CallTestSuiteRunner;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner.CustomTestSuiteRunner;

/**
 * Various tester and helper functions for the Runner classes.
 */
public class RunnerService {
    private static final Logger LOGGER = LogManager.getLogger(RunnerService.class.getName());

    /**
     * Test a single endpoint and return report
     *
     * @param ep             Resource to be tested
     * @param testCollection Test to be run
     * @return Report
     */
    public static TestSuiteReport testEndpoint(Resource ep, TestCollection testCollection, boolean allowAdditional) {
        String id = "";
        if (ep.getId() != null) {
            id = ep.getId().toString();
        }
        CustomTestSuiteRunner t = new CustomTestSuiteRunner(id, ep.getUrl(), testCollection);
        return t.runTests(allowAdditional);
    }
    
    /**
     * Test a single endpoint with call and return report
     *
     * @param ep             Resource to be tested
     * @param tc 
     * @return Report
     */
	public static TestSuiteReport testEndpointWithCall(Resource ep, TestCollection tc, boolean allowAdditional) {
        String id = "";
        if (ep.getId() != null) {
            id = ep.getId().toString();
        }
        CallTestSuiteRunner t = new CallTestSuiteRunner(id, ep.getUrl(), tc);
        return t.runTests(allowAdditional);
    }

    /**
     * Test all endpoints with a certain frequency set in database
     *
     * @param testCollection Config instance.
     * @param frequency      Get endpoints with this frequency.
     * @return List of TestSuiteReport containing the test results.
     * @throws SQLException SQL connection
     */
    public static int TestAllEndpointsWithFreq(TestCollection testCollection, String frequency, boolean allowAdditional) throws SQLException {
        List<Resource> l = ResourceService.getAllEndpointsWithFreq(frequency);
        LOGGER.info("Endpoints found: " + l.size());
        int count = 0;
        boolean sent;
        for (int i = 0; i < l.size(); i++) {
            Resource resource = l.get(i);
            TestSuiteReport testSuiteReport = RunnerService.testEndpointWithCall(resource, testCollection, allowAdditional);
            
            final int N = resource.getStoreprev();
            // Get last N reports
            List<TestReport> prevReports = TestReportService.getLastReports(resource, N);
            if (prevReports.size() >= N) {
            	TestReportService.deleteOlderThan(prevReports.get(prevReports.size()-1)); //Delete older than last N.
            }
            
            
            ObjectMapper mapper = new ObjectMapper();
            try {
            	TestReport report = new TestReport(resource, mapper.writeValueAsString(testSuiteReport));
                String reportId = TestReportService.saveReport(report);
                testSuiteReport.setId(reportId);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Unable to save report:" + e.getMessage());
            }
            EmailManager manager = new EmailManager(resource);
            manager.setPrevReports(prevReports);
            sent = manager.sendReport(testSuiteReport);
            if (sent) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * Replace all variable placeholders in a string.
     *
     * @param s  String
     * @param vs VariableStorage instance containing the variables.
     * @return The string with placeholders replaced for variable values.
     */
    public static String replaceVariables(String s, VariableStorage vs) {
        StrSubstitutor sub = new StrSubstitutor(vs.getVariables(), "{", "}");
        return sub.replace(s);
    }

    /**
     * Replace all variable placeholders in a string to be used as Url. This means mostly that
     * strings are replaced without including the quotes (otherwise they would be JSON which contains quotes for strings).
     *
     * @param s  String
     * @param vs VariableStorage instance containing the variables.
     * @return The string with placeholders replaced for variable values.
     */
    public static String replaceVariablesUrl(String s, VariableStorage vs) {
        StrSubstitutor sub = new StrSubstitutor(vs.getVariablesAsString(), "{", "}");
        return sub.replace(s);
    }

    /**
     * Run tests on a single endpoint.
     *
     * @param simple Item that contains the endpoints and tests to be run.
     * @return Test run report.
     */
    public static TestItemReport singleTestEndpoint(Item simple, boolean allowAdditional) {
        TestItemRunner tir = new TestItemRunner(simple);
        return tir.runTests(allowAdditional);
    }

    /**
     * Run tests on a single endpoint
     *
     * @param simple  Item that contains the endpoints and tests to be run.
     * @param storage Variables to use during tests.
     * @return Test run report.
     */
    public static TestItemReport singleTestEndpoint(Item simple, VariableStorage storage, boolean allowAdditional) {
        TestItemRunner tir = new TestItemRunner(simple, storage);
        return tir.runTests(allowAdditional);
    }

	public static void TestAllPublicEndpoints(TestCollection tc, boolean allowAdditional) throws SQLException {
		List<Resource> l = ResourceService.getAllPublicEndpoints();
		LOGGER.info("Endpoints found: " + l.size());
		for (int i = 0; i < l.size(); i++) {
			Resource resource = l.get(i);
            TestSuiteReport testSuiteReport = RunnerService.testEndpointWithCall(resource, tc, allowAdditional);
            
            ObjectMapper mapper = new ObjectMapper();
            try {
            	TestReport report = new TestReport(resource, mapper.writeValueAsString(testSuiteReport));
                String reportId = TestReportService.saveReport(report);
                testSuiteReport.setId(reportId);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Unable to save report:" + e.getMessage());
            }
		}
		
	}

	public static void TestEndpointWithCallAndSaveReport(Resource res, TestCollection tc, boolean allowAdditional) throws JsonProcessingException, SQLException {
		ObjectMapper mapper = new ObjectMapper();
		TestSuiteReport testSuiteReport = RunnerService.testEndpointWithCall(res, tc, allowAdditional);
        TestReport report = new TestReport(res, mapper.writeValueAsString(testSuiteReport));
        String reportId = TestReportService.saveReport(report);
        testSuiteReport.setId(reportId);
		
	}

}
