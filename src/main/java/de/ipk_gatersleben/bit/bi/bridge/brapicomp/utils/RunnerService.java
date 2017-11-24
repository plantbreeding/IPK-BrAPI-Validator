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
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner.TestSuiteRunner;

/**
 * Various tester and helper functions for the Runner classes.
 */
public class RunnerService {
    private static final Logger LOGGER = LogManager.getLogger(RunnerService.class.getName());

    /**
     * Test a single endpoint and return report
     *
     * @param ep             Endpoint to be tested
     * @param testCollection Test to be run
     * @return Report
     */
    public static TestSuiteReport testEndpoint(Endpoint ep, TestCollection testCollection) {
        String id = "";
        if (ep.getId() != null) {
            id = ep.getId().toString();
        }
        TestSuiteRunner t = new TestSuiteRunner(id, ep.getUrl(), testCollection);
        return t.runTests();
    }

    /**
     * Test all endpoints with a certain frequency set in database
     *
     * @param testCollection Config instance.
     * @param frequency      Get endpoints with this frequency.
     * @return List of TestSuiteReport containing the test results.
     * @throws SQLException SQL connection
     */
    public static int TestAllEndpointsWithFreq(TestCollection testCollection, String frequency) throws SQLException {
        List<Endpoint> l = EndpointService.getAllEndpointsWithFreq(frequency);
        LOGGER.info("Endpoints found: " + l.size());
        int count = 0;
        boolean sent;
        for (int i = 0; i < l.size(); i++) {
            Endpoint endpoint = l.get(i);
            TestSuiteReport testSuiteReport = RunnerService.testEndpoint(endpoint, testCollection);
            
            final int N = endpoint.getStoreprev();
            // Get last N reports
            List<TestReport> prevReports = TestReportService.getLastReports(endpoint, N);
            if (prevReports.size() >= N) {
            	TestReportService.deleteOlderThan(prevReports.get(prevReports.size()-1)); //Delete older than last N.
            }
            
            
            ObjectMapper mapper = new ObjectMapper();
            try {
            	TestReport report = new TestReport(endpoint, mapper.writeValueAsString(testSuiteReport));
                String reportId = TestReportService.saveReport(report);
                testSuiteReport.setId(reportId);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Unable to save report:" + e.getMessage());
            }
            EmailManager manager = new EmailManager(endpoint);
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
    public static TestItemReport singleTestEndpoint(Item simple) {
        TestItemRunner tir = new TestItemRunner(simple);
        return tir.runTests();
    }

    /**
     * Run tests on a single endpoint
     *
     * @param simple  Item that contains the endpoints and tests to be run.
     * @param storage Variables to use during tests.
     * @return Test run report.
     */
    public static TestItemReport singleTestEndpoint(Item simple, VariableStorage storage) {
        TestItemRunner tir = new TestItemRunner(simple, storage);
        return tir.runTests();
    }

}
