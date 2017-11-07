package de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.text.StrSubstitutor;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.Endpoint;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.EndpointService;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities.User;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.Item;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config.TestCollection;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestItemReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.VariableStorage;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner.TestItemRunner;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.runner.TestSuiteRunner;

/**
 * Various tester and helper functions for the Runner classes.
 *
 */
public class RunnerService {
	private static final Logger LOGGER = Logger.getLogger(RunnerService.class.getName());
		
	public static TestSuiteReport testEndpoint(Endpoint ep, TestCollection testCollection) {
		TestSuiteRunner t = new TestSuiteRunner(ep.getId(),ep.getUrl(), testCollection);
		return t.runTests();
	}
	
	/**
	 * Tests all endpoints belonging to a user.
	 * @param u User
	 * @param testCollection Config instance.
	 * @return List of TestSuiteReport containing the test results.
	 * @throws SQLException SQL connection
	 */
	public static List<TestSuiteReport> testAllUserEndpoints(User u, TestCollection testCollection) throws SQLException {
		List<Endpoint> l = EndpointService.getEndpointsFromUser(u);
				
		LOGGER.info("Endpoints found: "+ l.size());
		List<TestSuiteReport> testSuiteList = new ArrayList<TestSuiteReport>();
		for (int i = 0; i < l.size(); i++) {
			Endpoint ep = l.get(i);
			testSuiteList.add(testEndpoint(ep, testCollection));
		}
		return testSuiteList;
	}
	

	/**
	 * Test all endpoints in the database
	 * @param testCollection Config instance.
	 * @return List of TestSuiteReport containing the test results.
	 * @throws SQLException SQL connection
	 */
	public static List<TestSuiteReport> TestAllEndpoints(TestCollection testCollection) throws SQLException {
		List<Endpoint> l = EndpointService.getAllEndpoints();
			
		LOGGER.info("Endpoints found: " + l.size());
		List<TestSuiteReport> testSuiteList = new ArrayList<TestSuiteReport>();
		for (int i = 0; i < l.size(); i++) {
			Endpoint ep = l.get(i);
			testSuiteList.add(testEndpoint(ep, testCollection));
		}
		return testSuiteList;
	}
	
	/**
	 * Replace all variable placeholders in a string.
	 * @param s String
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
	 * @param s String
	 * @param vs VariableStorage instance containing the variables.
	 * @return The string with placeholders replaced for variable values.
	 */
	public static String replaceVariablesUrl(String s, VariableStorage vs) {
		StrSubstitutor sub = new StrSubstitutor(vs.getVariablesAsString(), "{", "}");
		return sub.replace(s);
	}

	/**
	 * Run tests on a single endpoint.
	 * @param simple Item that contains the endpoints and tests to be run.
	 * @return Test run report.
	 */
	public static TestItemReport singleTestEndpoint(Item simple) {
		TestItemRunner tir = new TestItemRunner(simple);
		return tir.runTests();
	}

	/**
	 * Run tests on a single endpoint
	 * @param simple Item that contains the endpoints and tests to be run.
	 * @param storage Variables to use during tests.
	 * @return Test run report.
	 */
	public static TestItemReport singleTestEndpoint(Item simple, VariableStorage storage) {
		TestItemRunner tir = new TestItemRunner(simple, storage);
		return tir.runTests();
	}
	
}
