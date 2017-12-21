package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.util.List;
import java.util.UUID;

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
    
    @DatabaseField(generatedId = true, columnName = NAME_FIELD_NAME)
    private String name;
    
    @DatabaseField(generatedId = true, columnName = DESCRIPTION_FIELD_NAME)
    private String description;
    
    @DatabaseField(generatedId = true, columnName = LOGO_FIELD_NAME)
    private String logo;
    
    @ForeignCollectionField(columnName = RESOURCES_FIELD_NAME, eager = true)
    private List<Resource> resources;
    

}
