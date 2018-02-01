package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Cache;
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
	 * @param resource
	 * @param last Number of reports to get.
	 * @return latest endpoints
	 * @throws SQLException 
	 */
	public static List<TestReport> getLastReports(Resource resource, int last) throws SQLException {
		Dao<TestReport, UUID> testReportDao = DataSourceManager.getDao(TestReport.class);
		Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
		List<TestReport> trl;
		
		// Patch because .limit() doesn't work on ormlite with oracle.
		if (Cache.getFromCache("dbType").equals("oracle")) {
			QueryBuilder<TestReport, UUID> qb = testReportDao.queryBuilder();
			qb.where().eq(TestReport.RESOURCE_FIELD_NAME, resource);
			qb.orderBy(TestReport.DATE_FIELD_NAME, false); //Descending
			trl = qb.query();
			last = Math.min(last, trl.size());
			trl = trl.subList(0, last); //Artificial limit.
			
		} else { //Non-oracle dbs
			QueryBuilder<TestReport, UUID> qb = testReportDao.queryBuilder();
			qb.where().eq(TestReport.RESOURCE_FIELD_NAME, resource);
			qb.orderBy(TestReport.DATE_FIELD_NAME, false).limit((long) last); //Descending
			
			trl = qb.query();
		}
		
		trl.forEach(tr -> {
			try {
				endpointDao.refresh(tr.getResource());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		return trl;
	}
	
	/**
	 * Delete reports older than a specific one
	 * @param testReport Reports older than this one will get deleted from the database.
	 * @throws SQLException 
	 */
	public static void deleteOlderThan(TestReport testReport) throws SQLException {
		Dao<TestReport, UUID> testReportDao = DataSourceManager.getDao(TestReport.class);
		DeleteBuilder<TestReport, UUID> qb = testReportDao.deleteBuilder();
		qb.where().eq(TestReport.RESOURCE_FIELD_NAME, testReport.getResource())
			.and().lt(TestReport.DATE_FIELD_NAME, testReport.getDate());
		qb.delete();
	}

	public static List<TestReport> getAllPublicEndpointLastReport() throws SQLException {
		List<Resource> l = ResourceService.getAllPublicEndpoints();
		List<TestReport> lastReports = new ArrayList<TestReport>();
		l.forEach(endpoint -> {
			try {
				List<TestReport> tr = getLastReports(endpoint, 1);
				if (tr.size() > 0 && tr.get(0) != null) {
					lastReports.add(tr.get(0));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		return lastReports;
	}
	
	public static Map<String, List<TestReport>> getAllPublicEndpointLastReports(int last) throws SQLException {
		List<Resource> l = ResourceService.getAllPublicEndpoints();
		Collections.sort(l);
		Map<String, List<TestReport>> lastReports = new LinkedHashMap<String, List<TestReport>>();
		l.forEach(endpoint -> {
			try {
				List<TestReport> tr = getLastReports(endpoint, last);
				lastReports.put(endpoint.getId().toString(), tr);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		return lastReports;
	}
}

