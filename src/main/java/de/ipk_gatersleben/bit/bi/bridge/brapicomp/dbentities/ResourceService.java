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
public class ResourceService {

    /**
     * Get a list of all endpoints that belong to an email.
     *
     * @param email Email to search endpoints for
     * @return List of endpoints that belong to that email
     * @throws SQLException SQL Error.
     */
    public static List<Resource> getEndpointsWithEmail(String email) throws SQLException {
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
        List<Resource> l = endpointDao.queryBuilder().where()
                .eq(Resource.EMAIL_FIELD_NAME, email)
                .and()
                .eq(Resource.ISPUBLIC_FIELD_NAME, false)
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
    public static Resource getEndpointWithEmailAndUrlAndFreq(String email, String url, String freq) throws SQLException {
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
        Resource e = endpointDao.queryBuilder().where()
                .eq(Resource.EMAIL_FIELD_NAME, email).and()
                .eq(Resource.URL_FIELD_NAME, url).and()
                .eq(Resource.FREQUENCY_FIELD_NAME, freq).and()
                .eq(Resource.CONFIRMED_FIELD_NAME, true)
                .and()
                .eq(Resource.ISPUBLIC_FIELD_NAME, false)
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
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
        Resource e = endpointDao.queryForId(UUID.fromString(endpointId));
        if (e.isPublic()) {
        	return false;
        }
        int d = endpointDao.deleteById(UUID.fromString(endpointId));
        if (d == 0) {
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
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
        DeleteBuilder<Resource, UUID> db = endpointDao.deleteBuilder();
        db.where()
	        .eq(Resource.EMAIL_FIELD_NAME, email).and()
	        .eq(Resource.CONFIRMED_FIELD_NAME, true).and()
	        .eq(Resource.ISPUBLIC_FIELD_NAME, false);
        db.delete();
                

    }

    /**
     * Get all active endpoints with a certain frequency.
     * 
     * @param freq
     * @return List of endpoints
     * @throws SQLException
     */
    public static List<Resource> getAllEndpointsWithFreq(String freq) throws SQLException {
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
        List<Resource> l = endpointDao.queryBuilder().where()
                .eq(Resource.FREQUENCY_FIELD_NAME, freq).and()
                .eq(Resource.CONFIRMED_FIELD_NAME, true).and()
    	        .eq(Resource.ISPUBLIC_FIELD_NAME, false)
                .query();
        return l;
    }
    
    public static Boolean confirmEndpointWithId(String endpointId) throws SQLException {
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
        Resource e = endpointDao.queryForId(UUID.fromString(endpointId));
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
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
        Resource e = endpointDao.queryBuilder().where()
        		.eq(Resource.ID_FIELD_NAME, UUID.fromString(endpointId))
        		.and()
        		.eq(Resource.ISPUBLIC_FIELD_NAME, false).queryForFirst();
        if (e == null) {
            // Not found.
            return null;
        }
        e.setFrequency(frequency);
        endpointDao.update(e);
        return true;
	}

	public static List<Resource> getAllEndpoints() throws SQLException {
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
        List<Resource> l = endpointDao.queryForAll();
        return l;
	}
	
	public static Resource getPublicEndpoint(String id) throws SQLException {
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
        return endpointDao.queryBuilder().where()
        		.eq(Resource.ID_FIELD_NAME, UUID.fromString(id))
        		.and()
        		.eq(Resource.ISPUBLIC_FIELD_NAME, true)
        		.queryForFirst();
	}

	public static List<Resource> getAllPublicEndpoints() throws SQLException {
        Dao<Resource, UUID> endpointDao = DataSourceManager.getDao(Resource.class);
        return endpointDao.queryBuilder().where()
        		.eq(Resource.ISPUBLIC_FIELD_NAME, true)
        		.query();
	}
	
}
