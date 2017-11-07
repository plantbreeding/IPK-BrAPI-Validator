package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * User - table model
 * A user instance contains the information related to one user (username, email and apikey)
 * It represents the "users" table.
 */

@DatabaseTable(tableName = "users")
public class User {
	
	public static final String USERNAME_FIELD_NAME = "USERNAME";

	public static final String ID_FIELD_NAME = "ID";

	@DatabaseField(generatedId = true)
	private UUID internalId;
	
	@DatabaseField(unique = false, canBeNull = false, columnName = USERNAME_FIELD_NAME)
	private String username;
	
	@DatabaseField(canBeNull = false)
	private String email;
	
	@DatabaseField(canBeNull = false)
	private UUID apiKey;
	
	public User() {}
	
	/**
	 * @return username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @param username Username
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @return email
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * @param email Email address.
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Generate a random API key for the user 
	 */
	public void generateApiKey() {
		this.apiKey = UUID.randomUUID();
	}
	/**
	 * @return API key
	 */
	public String getApiKey() {
		return this.apiKey.toString();
	}

	/**
	 * Test an API key against the user's
	 * @param tested API key to be tested against the user's
	 * @return True if the tested key is equal to the user's
	 */
	public boolean checkApiKey(String tested) {
		return tested.equals(this.apiKey.toString());
	}

	/**
	 * @return Internal ID (not username)
	 */
	@JsonIgnore
	public UUID getInternalIdUUID() {
		return this.internalId;
	}


}
