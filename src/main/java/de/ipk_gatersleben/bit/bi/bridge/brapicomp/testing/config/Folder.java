package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config;

import java.util.List;

/**
 * A folder contains multiple Items, which contain the tests themselves.
 * Folders are used to conceptually organize tests.
 */
public class Folder {
    private String name;
    private String description;
    private List<Item> item;
    private List<Variable> variable;

    public Folder() {
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

    public List<Item> getItem() {
        return item;
    }

    public void setItem(List<Item> item) {
        this.item = item;
    }

    public List<Variable> getVariable() {
        return variable;
    }

    /**
     * @param variable Variables to provide for the tests
     */
    public void setVariable(List<Variable> variable) {
        this.variable = variable;
    }
}
