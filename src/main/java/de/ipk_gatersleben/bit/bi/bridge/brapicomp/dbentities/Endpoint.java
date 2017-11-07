package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Endpoint - table model
 * An endpoint instance contains the information related to one endpoint (url, and user it belongs to)
 * It represents the "endpoints" table.
 */
@DatabaseTable(tableName = "endpoints")
public class Endpoint {
	
	public static final String USER_FIELD_NAME = "USER_ID";

	public static final String URL_FIELD_NAME = "URL";

	public static final String ID_FIELD_NAME = "ID";
	
	@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
	private int id;
	
	@DatabaseField(canBeNull = false, columnName = URL_FIELD_NAME)
	private String url;
	
	@DatabaseField(canBeNull = false, foreign = true, columnName = USER_FIELD_NAME)
	private User user;
	
	public Endpoint() {
	}
	
	public Endpoint(String url) {
		this.url = url;
	}

	/**
	 * @return endpoint's id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return endpoint's url
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * @param u Endpoint's owner User.
	 */
	public void setUser(User u) {
		this.user = u;
	}
}
