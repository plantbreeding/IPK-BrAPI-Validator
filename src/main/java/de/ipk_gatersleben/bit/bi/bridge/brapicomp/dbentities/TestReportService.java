package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports.TestSuiteReport;
import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;

/**
 * Service class with static methods to interact with the database. Get/delete test reports and so on.
 */
public class TestReportService {

    /**
     * Save a test report belonging to an endpoint
     *
     * @param endpoint Tested endpoint
     * @param reportJson String with serialized report
     * @return List of endpoints that belong to that email
     * @throws SQLException SQL Error.
     * @throws JsonProcessingException 
     */
    public static String saveReport(TestReport testReport) throws SQLException {
        Dao<TestReport, UUID> testReportDao = DataSourceManager.getDao(TestReport.class);
        testReportDao.create(testReport);
        return testReport.getReportId().toString();
    }

    /**
     * Get a report with ID
     * @param reportId
     * @return TestReport
     * @throws SQLException
     */

    public static TestReport getReport(String reportId) throws SQLException {
        Dao<TestReport, UUID> testReportDao = DataSourceManager.getDao(TestReport.class);
        return testReportDao.queryForId(UUID.fromString(reportId));
    }

	/**
	 * Get the last n reports for a given endpoint in descending order (newest first)
	 * @param endpoint
	 * @param last Number of reports to get.
	 * @return latest endpoints
	 * @throws SQLException 
	 */
	public static List<TestReport> getLastReports(Endpoint endpoint, long last) throws SQLException {
		Dao<TestReport, UUID> testReportDao = DataSourceManager.getDao(TestReport.class);
		QueryBuilder<TestReport, UUID> qb = testReportDao.queryBuilder();
		qb.where().eq(TestReport.ENDPOINTID_FIELD_NAME, endpoint);
		qb.orderBy(TestReport.DATE_FIELD_NAME, false).limit(last); //Descending
		return qb.query();
	}
}
