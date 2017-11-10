package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;

/**
 * Service class with static methods to interact with the database. Get/delete endpoints and so on.
 *
 */
public class EndpointService {
	
	/**
	 * Get a list of all endpoints that belong to an email.
	 * @param u User to search endpoints for
	 * @return List of endpoints that belong to that email
	 * @throws SQLException SQL Error.
	 */
	public static List<Endpoint> getEndpointsWithEmail(String email) throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		List<Endpoint> l = endpointDao.queryBuilder().where()
				.eq(Endpoint.EMAIL_FIELD_NAME, email).and()
				.eq(Endpoint.DELETED_FIELD_NAME, false)
				.query();
		return l;
	}
	
	/**
	 * Get a list of all endpoints that belong to an email.
	 * @param u User to search endpoints for
	 * @return List of endpoints that belong to that email
	 * @throws SQLException SQL Error.
	 */
	public static Endpoint getEndpointWithEmailAndUrl(String email, String url) throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		Endpoint e = endpointDao.queryBuilder().where()
				.eq(Endpoint.EMAIL_FIELD_NAME, email).and()
				.eq(Endpoint.URL_FIELD_NAME, url).and()
				.eq(Endpoint.DELETED_FIELD_NAME, false)
				.queryForFirst();
		return e;
	}
	

	/**
	 * Delete endpoint
	 * @param endpointId Delete endpoint with this id
	 * @throws SQLException SQL Error.
	 */
	public static boolean deleteEndpointWithId(String endpointId) throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		Endpoint e = endpointDao.queryForId(UUID.fromString(endpointId));
		if (e == null) {
			// Not found.
			return false; 
		}
		e.setDeleted(true);
		endpointDao.update(e);
		return true;
	}
	
	
	/**
	 * Delete all endpoints that belong to a user
	 * @param u User to delete endpoints of
	 * @throws SQLException SQL Error.
	 */
	public static void deleteAllEmailEndpoints(String email) throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		List<Endpoint> e = endpointDao.queryBuilder().where()
				.eq(Endpoint.EMAIL_FIELD_NAME, email).and()
				.eq(Endpoint.DELETED_FIELD_NAME, false)
				.query();
		for (int i = 0; i < e.size(); i++) {
			e.get(i).setDeleted(true);
			endpointDao.update(e.get(i));
		}
	}

	public static List<Endpoint> getAllEndpointsWithFreq(String freq) throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		List<Endpoint> l = endpointDao.queryBuilder().where()
					.eq(Endpoint.FREQUENCY_FIELD_NAME, freq).and()
					.eq(Endpoint.DELETED_FIELD_NAME, false)
					.query();
				;
		return l;
	}
	
	public static List<Endpoint> getAllEndpoints() throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		List<Endpoint> l = endpointDao.queryBuilder().where()
					.eq(Endpoint.DELETED_FIELD_NAME, false)
					.query();
		return l;
	}
}
