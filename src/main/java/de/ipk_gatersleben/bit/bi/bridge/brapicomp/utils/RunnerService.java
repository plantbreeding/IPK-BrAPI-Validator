package de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.TestReportService;
import org.apache.commons.lang3.text.StrSubstitutor;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.ci.EmailManager;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.EndpointService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Item;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestItemReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.VariableStorage;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner.TestItemRunner;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner.TestSuiteRunner;

/**
 * Various tester and helper functions for the Runner classes.
 */
public class RunnerService {
    private static final Logger LOGGER = Logger.getLogger(RunnerService.class.getName());

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
     * Test multiple endpoints
     *
     * @param eps            Endpoint list
     * @param testCollection Test to be run
     * @return List of reports
     */
    private static List<TestSuiteReport> testEndpoints(List<Endpoint> eps, TestCollection testCollection) {
        List<TestSuiteReport> testSuiteList = new ArrayList<>();
        eps.forEach(endpoint -> testSuiteList.add(testEndpoint(endpoint, testCollection)));

        return testSuiteList;

    }

    /**
     * Tests all endpoints belonging to an email.
     *
     * @param email          Email
     * @param testCollection Config instance.
     * @return List of TestSuiteReport containing the test results.
     * @throws SQLException SQL connection
     */
    public static List<TestSuiteReport> testAllEmailEndpoints(String email, TestCollection testCollection) throws SQLException {
        List<Endpoint> l = EndpointService.getEndpointsWithEmail(email);
        LOGGER.info("Endpoints found: " + l.size());
        return testEndpoints(l, testCollection);
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
            ObjectMapper mapper = new ObjectMapper();
            try {
                String reportId = TestReportService.saveReport(endpoint, mapper.writeValueAsString(testSuiteReport));
                testSuiteReport.setId(reportId);
            } catch (JsonProcessingException e) {
                LOGGER.warning("Unable to save report:" + e.getMessage());
            }
             sent = new EmailManager(endpoint).sendReport(testSuiteReport);
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
