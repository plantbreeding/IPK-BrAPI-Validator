package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.utils.RandomString;

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
	private UUID salt;

	@DatabaseField(canBeNull = false)
	private String hashed;
	
	private String apiKey;
	
	@DatabaseField()
	private String role = "USER";
	
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

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
		this.salt = UUID.randomUUID();
		this.apiKey = new RandomString(12).nextString();
		this.hashed = Hashing.sha256()
						.hashString(salt.toString() + apiKey, StandardCharsets.UTF_8)
						.toString();
	}
	
	/**
	 * Returns API Key.
	 * @return apiKey
	 */
	public String getApiKey() {
		return apiKey;
	}
	
	/**
	 * Check if a given apiKey is correct
	 * @return True if API key is valid.
	 */
	public boolean checkApiKey(String apiKey) {
		return this.hashed.equals(Hashing.sha256()
				.hashString(this.salt.toString() + apiKey, StandardCharsets.UTF_8)
				.toString());
	}

	/**
	 * @return Internal ID (not username)
	 */
	@JsonIgnore
	public UUID getInternalIdUUID() {
		return this.internalId;
	}


}
