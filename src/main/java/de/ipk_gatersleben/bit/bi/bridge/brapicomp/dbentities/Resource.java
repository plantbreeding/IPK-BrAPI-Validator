package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.fasterxml.jackson.annotation.*;

import de.ipk_gatersleben.bit.bi.bridge.brapicomp.Config;

/**
 * Resource - table model An endpoint instance contains the information related
 * to one endpoint (url, and user it belongs to) It represents the "endpoints"
 * table.
 */
@DatabaseTable(tableName = "resources")
public class Resource implements Comparable<Resource> {

	public static final String EMAIL_FIELD_NAME = "EMAIL";

	public static final String URL_FIELD_NAME = "URL";

	public static final String ID_FIELD_NAME = "ID";

	public static final String CROP_FIELD_NAME = "CROP";

	public static final String FREQUENCY_FIELD_NAME = "FREQUENCY";

	public static final String CONFIRMED_FIELD_NAME = "CONFIRMED";

	public static final String STOREPREV_FIELD_NAME = "STOREPREV";

	public static final String ISPUBLIC_FIELD_NAME = "ISPUBLIC";

	public static final String NAME_FIELD_NAME = "NAME";

	public static final String DESCRIPTION_FIELD_NAME = "DESCRIPTION";

	public static final String SUBMITTOREPO_FIELD_NAME = "SUBMIT_TO_REPO";

	public static final String PROVIDER_FIELD_NAME = "PROVIDER";

	public static final String CERTIFICATE_FIELD_NAME = "CERTIFICATE";

	public static final String LOGO_FIELD_NAME = "LOGO";

	@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
	private UUID id;

	@JsonProperty("base-url")
	@DatabaseField(canBeNull = false, columnName = URL_FIELD_NAME)
	private String url;

	@DatabaseField(columnName = CROP_FIELD_NAME)
	private String crop;

	@DatabaseField(columnName = EMAIL_FIELD_NAME) // Null controlled in resource
	private String email;

	@DatabaseField(canBeNull = false, columnName = FREQUENCY_FIELD_NAME)
	private String frequency = "weekly";

	@DatabaseField(format = "integer", canBeNull = false, columnName = CONFIRMED_FIELD_NAME)
	private boolean confirmed = false;

	@DatabaseField(canBeNull = false, columnName = STOREPREV_FIELD_NAME)
	private int storeprev = 3;

	@JsonIgnore
	@DatabaseField(format = "integer", canBeNull = false, columnName = ISPUBLIC_FIELD_NAME)
	private boolean isPublic = false;

	@JsonIgnore
	@DatabaseField(format = "integer", canBeNull = false, columnName = SUBMITTOREPO_FIELD_NAME)
	private boolean submitToRepo = false;

	@DatabaseField(columnName = NAME_FIELD_NAME)
	private String name = ""; // Only used for public endpoints

	@DatabaseField(columnName = DESCRIPTION_FIELD_NAME)
	private String description = ""; // Only used for public endpoints

	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = PROVIDER_FIELD_NAME)
	private Provider provider;

	@DatabaseField(columnName = CERTIFICATE_FIELD_NAME)
	private String certificate = ""; // Only used for public endpoints

	@DatabaseField(columnName = LOGO_FIELD_NAME)
	private String logo = ""; // Only used for public endpoints

	@Override
	public int compareTo(Resource other) {
	    return this.name.compareTo(other.name); //Sort alphabetically by name.
	}
	
	public Resource() {
	}

	public Resource(String url) throws MalformedURLException {
		this.setUrl(url);
	}

	public Resource(String u, String e, String f) throws IllegalArgumentException, MalformedURLException {
		this.setUrl(u);
		this.setEmail(e);
		this.setFrequency(f);
	}

	/**
	 * @return endpoint's id
	 */
	public UUID getId() {
		return id;
	}

	public void setUrl(String url) throws MalformedURLException {
		URL u = new URL(url);
		if ((Config.get("advancedMode") != null && Config.get("advancedMode").equals("true"))
			&& u.getPort() != 80 && u.getPort() != -1) {
			throw new IllegalArgumentException();
		}
		this.url = u.toString();
	}

	/**
	 * @return endpoint's url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param e
	 *            Resource's owner email.
	 */
	@JsonProperty
	public void setEmail(String e) {
		this.email = e;
	}

	/**
	 * @return e Resource's email
	 */
	@JsonIgnore
	public String getEmail() {
		return email;
	}

	/**
	 * @param f
	 *            Resource's testing frequency.
	 */
	public void setFrequency(String f) throws IllegalArgumentException {
		if (f.equals("weekly") || f.equals("monthly") || f.equals("daily")) {
			this.frequency = f;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public String getFrequency() {
		return frequency;
	}

	public void setConfirmed(boolean b) {
		this.confirmed = b;
	}

	@JsonIgnore
	public boolean isConfirmed() {
		return confirmed;
	}

	@JsonIgnore
	public int getStoreprev() {
		return storeprev;
	}

	public void setStoreprev(int storeprev) {
		if (storeprev == 3) { // Only three is valid for now
			this.storeprev = storeprev;
		} else {
			throw new IllegalArgumentException();
		}
	}

	@JsonIgnore
	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public String getCrop() {
		return crop;
	}

	public void setCrop(String crop) {
		this.crop = crop;
	}

	@JsonIgnore
	public boolean isSubmitToRepo() {
		return submitToRepo;
	}

	@JsonProperty
	public void setSubmitToRepo(boolean submitToRepo) {
		this.submitToRepo = submitToRepo;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}
	
	public List<TestReport> getLastTestReports() {
		try {
			return TestReportService.getLastReports(this, 3);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
