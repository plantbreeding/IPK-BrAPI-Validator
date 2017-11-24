package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;

/**
 * Service class with static methods to interact with the database. Get/delete endpoints and so on.
 */
public class EndpointService {

    /**
     * Get a list of all endpoints that belong to an email.
     *
     * @param email Email to search endpoints for
     * @return List of endpoints that belong to that email
     * @throws SQLException SQL Error.
     */
    public static List<Endpoint> getEndpointsWithEmail(String email) throws SQLException {
        Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
        List<Endpoint> l = endpointDao.queryBuilder().where()
                .eq(Endpoint.EMAIL_FIELD_NAME, email)
                .query();
        return l;
    }

    /**
     * Get a list of all endpoints that belong to an email with specific URL and Freq
     *
     * @param email Email to search endpoints for
     * @param url   Url to search endpoints
     * @param freq  Frequency to search endpoints
     * @return List of endpoints that belong to that email
     * @throws SQLException SQL Error.
     */
    public static Endpoint getEndpointWithEmailAndUrlAndFreq(String email, String url, String freq) throws SQLException {
        Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
        Endpoint e = endpointDao.queryBuilder().where()
                .eq(Endpoint.EMAIL_FIELD_NAME, email).and()
                .eq(Endpoint.URL_FIELD_NAME, url).and()
                .eq(Endpoint.FREQUENCY_FIELD_NAME, freq).and()
                .eq(Endpoint.CONFIRMED_FIELD_NAME, true)
                .queryForFirst();
        return e;
    }

    /**
     * Delete endpoint
     *
     * @param endpointId Delete endpoint with this id
     * @throws SQLException SQL Error.
     */
    public static Boolean deleteEndpointWithId(String endpointId) throws SQLException {
        Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
        int e = endpointDao.deleteById(UUID.fromString(endpointId));
        if (e == 0) {
            // Not found.
            return false;
        }
        return true;
    }


    /**
     * Delete all endpoints that belong to a user
     *
     * @param email Email to delete endpoints of
     * @throws SQLException SQL Error.
     */
    public static void deleteAllEmailEndpoints(String email) throws SQLException {
        Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
        DeleteBuilder<Endpoint, UUID> db = endpointDao.deleteBuilder();
        db.where()
	        .eq(Endpoint.EMAIL_FIELD_NAME, email).and()
	        .eq(Endpoint.CONFIRMED_FIELD_NAME, true);
        db.delete();
                

    }

    /**
     * Get all active endpoints with a certain frequency.
     * 
     * @param freq
     * @return List of endpoints
     * @throws SQLException
     */
    public static List<Endpoint> getAllEndpointsWithFreq(String freq) throws SQLException {
        Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
        List<Endpoint> l = endpointDao.queryBuilder().where()
                .eq(Endpoint.FREQUENCY_FIELD_NAME, freq).and()
                .eq(Endpoint.CONFIRMED_FIELD_NAME, true)
                .query();
        return l;
    }
    
    public static Boolean confirmEndpointWithId(String endpointId) throws SQLException {
        Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
        Endpoint e = endpointDao.queryForId(UUID.fromString(endpointId));
        if (e == null) {
            // Not found.
            return null;
        }
        if (e.isConfirmed()) {
            return false;
        }
        e.setConfirmed(true);
        endpointDao.update(e);
        return true;
    }

	public static Boolean changeEndpointFreqWithId(String endpointId, String frequency) throws SQLException {
        Dao<Endpoint, UUID> endpointDao = DataSourceManager.getDao(Endpoint.class);
        Endpoint e = endpointDao.queryForId(UUID.fromString(endpointId));
        if (e == null) {
            // Not found.
            return null;
        }
        e.setFrequency(frequency);
        endpointDao.update(e);
        return true;
	}
}
