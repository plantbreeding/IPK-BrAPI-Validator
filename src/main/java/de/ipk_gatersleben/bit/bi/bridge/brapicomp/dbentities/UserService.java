package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.dao.Dao;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.DataSourceManager;


/**
 * Service class with static methods to interact with the database. Get/delete users and so on.
 *
 */
public class UserService {
	/**
	 * Check if a username exists in the database.
	 * @param username Username to be checked
	 * @return true if the username is taken
	 * @throws SQLException SQL Error.
	 */
	public static boolean usernameExists(String username) throws SQLException {
		Dao<User, UUID> userDao = DataSourceManager.getDao(User.class);
		List<User> l = userDao.queryBuilder().where().eq(User.USERNAME_FIELD_NAME, username)
			.query();
		if (l.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Get a user from the database given a username
	 * @param username User's username
	 * @return User User
	 * @throws SQLException SQL Error.
	 */
	public static User getUser(String username) throws SQLException {
		Dao<User, UUID> userDao = DataSourceManager.getDao(User.class);
		User u = userDao.queryBuilder().where().eq(User.USERNAME_FIELD_NAME, username).queryForFirst();
		return u;
	}

	/**
	 * Delete a user from the database
	 * @param u User to be deleted
	 * @throws SQLException SQL Error.
	 */
	public static void deleteUser(User u) throws SQLException {
		Dao<User, UUID> userDao = DataSourceManager.getDao(User.class);
		userDao.delete(u);
		
	}
}
