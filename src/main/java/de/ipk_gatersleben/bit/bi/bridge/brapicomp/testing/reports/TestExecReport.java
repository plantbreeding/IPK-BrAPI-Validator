package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.reports;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Report from one "exec" command.
 */
public class TestExecReport {
    private String name;
    private String type;
    private boolean passed;
    private List<String> message = new ArrayList<String>();
    private List<JsonNode> error = new ArrayList<JsonNode>();
    private String schema;

    public TestExecReport(String name, boolean passed) {
        this.setName(name);
        this.setPassed(passed);
    }

    public List<String> getMessage() {
        return message;
    }

    public void addMessage(String message) {
        this.message.add(message);
    }

    public boolean isPassed() {
        return passed;
    }


    public void setPassed(boolean passed) {
        this.passed = passed;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<JsonNode> getError() {
        return error.subList(0, Math.min(error.size(), 10));
    }


    public void addError(JsonNode error) {
        this.error.add(error);
    }

    /**
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @param schema the schema to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
