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
	 * Get a list of all endpoints that belong to a user.
	 * @param u User to search endpoints for
	 * @return List of endpoints that belong to that user
	 * @throws SQLException SQL Error.
	 */
	public static List<Endpoint> getEndpointsFromUser(User u) throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		List<Endpoint> l = endpointDao.queryBuilder().where().eq(Endpoint.USER_FIELD_NAME, u.getInternalIdUUID())
				.query();
		return l;
	}

	/**
	 * Delete all endpoints that belong to a user
	 * @param u User to delete endpoints of
	 * @throws SQLException SQL Error.
	 */
	public static void deleteAllUserEndpoints(User u) throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		List<Endpoint> e = endpointDao.queryBuilder().where()
				.eq(Endpoint.USER_FIELD_NAME, u.getInternalIdUUID())
				.query();
		for (int i = 0; i < e.size(); i++) {
			endpointDao.delete(e.get(i));
		}
		
	}
	
	/**
	 * Get a certain endpoint given a user and id
	 * @param u User that owns the endpoint
	 * @param id Endpoint's id
	 * @return Endpoint
	 * @throws SQLException SQL Error.
	 */
	public static Endpoint getUserEndpoint(User u, int id) throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		Endpoint e = endpointDao.queryBuilder().where()
				.eq(Endpoint.USER_FIELD_NAME, u.getInternalIdUUID())
				.and()
				.eq(Endpoint.ID_FIELD_NAME, id)
				.queryForFirst();
		return e;
		
	}

	/**
	 * Get a certain endpoint given a user and url
	 * @param u User that owns the endpoint
	 * @param url Endpoint's url
	 * @return Endpoint
	 * @throws SQLException SQL Error.
	 */
	public static Endpoint getUserEndpointWithUrl(User u, String url) throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		Endpoint e = endpointDao.queryBuilder().where()
				.eq(Endpoint.USER_FIELD_NAME, u.getInternalIdUUID())
				.and()
				.eq(Endpoint.URL_FIELD_NAME, url)
				.queryForFirst();
		return e;
	}

	public static List<Endpoint> getAllEndpoints() throws SQLException {
		Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
		List<Endpoint> l = endpointDao.queryForAll();
		return l;
	}
}
