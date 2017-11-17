package de.ipk_gatersleben.bit.bi.bridge.brapicomp.testing.config;

import java.util.List;

/**
 * Contains the list of tests for a given Item.
 */
public class Event {
    private String listen;
    private String type;
    private List<String> exec;

    public Event() {
    }

    public Event(List<String> exec) {
        setExec(exec);
    }

    public void setListen(String listen) {
        this.listen = listen;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * List of strings. Each string contains one command/test.
     *
     * @param exec List of exec instances.
     */
    public void setExec(List<String> exec) {
        this.exec = exec;
    }

    public String getListen() {
        return listen;
    }

    public String getType() {
        return type;
    }

    public List<String> getExec() {
        return exec;
    }


}
