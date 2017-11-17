package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config;

/**
 * Variable entity that contains some predefined variable coming from the test collection json file.
 * Currently the variables included in this object are not taken into account for the tests.
 */
public class Variable {
    private String id;
    private String key;
    private Object value;
    private String type;
    private String name;
    private String description;

    public Variable() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    private boolean disabled;
}
