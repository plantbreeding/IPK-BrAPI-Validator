package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.util.Collection;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Provider - table model
 * An provider instance contains the information related to one resource provider
 * It represents the "providers" table.
 */
@DatabaseTable(tableName = "providers")
public class Provider {
	
	public static final String NAME_FIELD_NAME = "NAME";
	
	public static final String DESCRIPTION_FIELD_NAME = "DESCRIPTION";
	
	public static final String LOGO_FIELD_NAME = "LOGO";

	public static final String ID_FIELD_NAME = "ID";
	
	public static final String RESOURCES_FIELD_NAME = "RESOURCES";

    @DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
    private UUID id;
    
    @DatabaseField(columnName = NAME_FIELD_NAME)
    private String name;
    
    @DatabaseField(dataType= DataType.LONG_STRING, columnName = DESCRIPTION_FIELD_NAME)
    private String description;
    
    @DatabaseField(columnName = LOGO_FIELD_NAME)
    private String logo;
    
    @JsonIgnore
    @ForeignCollectionField(columnName = RESOURCES_FIELD_NAME, eager = true)
    private Collection<Resource> resources;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}
	
	
	@JsonProperty("resources")
	@JsonIgnore
	public Collection<Resource> getResources() {
		return resources;
	}
	
	@JsonProperty("resources")
	public void setResources(Collection<Resource> resources) {
		this.resources = resources;
	}
    

}
