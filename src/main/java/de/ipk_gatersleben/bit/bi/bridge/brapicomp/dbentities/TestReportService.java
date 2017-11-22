package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;

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
     */
    public static String saveReport(Endpoint endpoint, String reportJson) throws SQLException {
        TestReport tr = new TestReport(endpoint, reportJson);
        Dao<TestReport, UUID> testReportDao = DataSourceManager.getDao(TestReport.class);
        testReportDao.create(tr);
        return tr.getReportId().toString();
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

    //Todo: Get latest n reports for endpoint.

}
