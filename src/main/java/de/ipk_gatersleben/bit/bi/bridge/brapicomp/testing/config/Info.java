package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config;

/**
 * Info object contains basic test collection information.
 */
public class Info {
	private String name;
	private String description;
	private String schema;
	public Info() {
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @param schema Schema is the string representing the schema version.
	 * Example: "http://json-schema.org/draft-04/schema#"
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getSchema() {
		return schema;
	}
	
}
