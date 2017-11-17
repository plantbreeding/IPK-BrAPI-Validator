package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config;

import java.util.List;

/**
 * Test collection configuration. Used to define which endpoints to call and tests to run.
 * The Json config file is deserialized into this.
 */
public class TestCollection {
    private Info info;
    private List<Folder> item;


    /**
     * Basic constructor
     */
    public TestCollection() {
    }

    /**
     * Set Info object
     *
     * @param info Info object containing basic information (name, description...)
     */
    public void setInfo(Info info) {
        this.info = info;
    }

    /**
     * The Item attribute contains the folders with the tests
     *
     * @param item Folder list
     */
    public void setItem(List<Folder> item) {
        this.item = item;
    }

    /**
     * @return Info object
     */
    public Info getInfo() {
        return info;
    }

    /**
     * @return Folder list
     */
    public List<Folder> getItem() {
        return item;
    }

}
